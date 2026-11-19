package br.senac.alocasenac.reserva;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import br.senac.alocasenac.auditoria.AuditoriaService;
import br.senac.alocasenac.notificacao.EstadoNotificacao;
import br.senac.alocasenac.notificacao.NotificacaoOutbox;
import br.senac.alocasenac.notificacao.NotificacaoOutboxRepository;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.RecursoRepository;

/**
 * Criação, alteração e exclusão de reservas (RF4, RF5, RF6, RN-E4, RN-E5).
 *
 * <p>Cada mudança roda em uma transação que grava reserva, ocupações, auditoria e outbox juntas (ADR-001, ADR-004,
 * ADR-005). A recusa é registrada depois, em transação própria (DP-06), para não segurar a conexão da transação
 * desfeita.
 */
@Service
public class ReservaService {

    static final String ENTIDADE = "Reserva";

    private final ReservaRepository reservas;
    private final OcupacaoRepository ocupacoes;
    private final VerificadorDisponibilidade verificador;
    private final AuditoriaService auditoria;
    private final RegistroDeRecusas recusas;
    private final NotificacaoOutboxRepository outbox;
    private final TransactionTemplate transacao;
    private final RecursoRepository recursos;
    private final Clock clock;

    public ReservaService(ReservaRepository reservas, OcupacaoRepository ocupacoes,
            VerificadorDisponibilidade verificador, AuditoriaService auditoria, RegistroDeRecusas recusas,
            NotificacaoOutboxRepository outbox, PlatformTransactionManager transacoes, RecursoRepository recursos,
            Clock clock) {
        this.reservas = reservas;
        this.ocupacoes = ocupacoes;
        this.verificador = verificador;
        this.auditoria = auditoria;
        this.recusas = recusas;
        this.outbox = outbox;
        this.transacao = new TransactionTemplate(transacoes);
        this.recursos = recursos;
        this.clock = clock;
    }

    public Reserva criar(String solicitante, Set<Long> recursoIds, Instant inicio, Instant fim) {
        Intervalo intervalo = Intervalo.de(inicio, fim);
        try {
            return transacao.execute(status -> gravarNova(solicitante, recursoIds, intervalo));
        } catch (ConflitoDeHorarioException conflito) {
            recusas.registrar(solicitante, recursoIds, inicio, fim, MotivoRecusa.SOBREPOSICAO);
            throw conflito;
        } catch (DataIntegrityViolationException concorrencia) {
            // Outra solicitação ocupou o horário entre a checagem prévia e a gravação: o banco recusou (I-4).
            recusas.registrar(solicitante, recursoIds, inicio, fim, MotivoRecusa.CONCORRENCIA);
            throw new ConflitoDeHorarioException(List.of());
        }
    }

    public Reserva alterar(Long reservaId, Instant inicio, Instant fim, String ator) {
        Intervalo novo = Intervalo.de(inicio, fim);
        try {
            return transacao.execute(status -> gravarAlteracao(reservaId, novo, ator));
        } catch (DataIntegrityViolationException concorrencia) {
            throw new ConflitoDeHorarioException(List.of());
        }
    }

    public void apagar(Long reservaId, String ator) {
        Reserva reserva = buscar(reservaId);
        reserva.verificarPodeSerApagada();
    }

    public Reserva buscar(Long reservaId) {
        return reservas.findById(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Reserva " + reservaId + " não encontrada."));
    }

    private Reserva gravarNova(String solicitante, Set<Long> recursoIds, Intervalo intervalo) {
        exigirDisponibilidade(recursoIds, intervalo, null);
        Reserva reserva = reservas.save(Reserva.nova(solicitante, recursoIds, intervalo));
        for (Long recursoId : recursoIds) {
            ocupacoes.save(new Ocupacao(recursoId, reserva.getId(), intervalo.inicio(), intervalo.fim(),
                    OrigemOcupacao.RESERVA, true));
        }
        ocupacoes.flush();
        auditoria.registrar(ENTIDADE, reserva.getId(), "CRIACAO", null, reserva.descricao(), solicitante);
        if (recursos.findAllById(recursoIds).stream().noneMatch(Recurso::isRestrito)) {
            // D-05: só recursos não restritos, o sistema aprova na hora (RF7 exige aprovação só para restritos)
            reserva.aplicar(AcaoReserva.APROVAR, clock.instant());
        }
        outbox.save(new NotificacaoOutbox("RESERVA_CRIADA", solicitante,
                "{\"reservaId\":" + reserva.getId() + "}", EstadoNotificacao.PENDENTE));
        return reserva;
    }

    private Reserva gravarAlteracao(Long reservaId, Intervalo novo, String ator) {
        Reserva reserva = buscar(reservaId);
        exigirDisponibilidade(reserva.getRecursoIds(), novo, reservaId);
        String anterior = reserva.descricao();
        reserva.reagendar(novo);
        for (Ocupacao ocupacao : ocupacoes.findByReferenciaIdAndOrigem(reservaId, OrigemOcupacao.RESERVA)) {
            ocupacao.reagendar(novo);
        }
        ocupacoes.flush();
        auditoria.registrar(ENTIDADE, reservaId, "ALTERACAO", anterior, reserva.descricao(), ator);
        return reservas.save(reserva);
    }

    private void exigirDisponibilidade(Set<Long> recursoIds, Intervalo intervalo, Long reservaIgnorada) {
        List<Ocupacao> conflitos = verificador.conflitos(recursoIds, intervalo, reservaIgnorada,
                ocupacoes.findByRecursoIdInAndAtivaTrue(recursoIds));
        if (!conflitos.isEmpty()) {
            throw new ConflitoDeHorarioException(conflitos);
        }
    }
}
