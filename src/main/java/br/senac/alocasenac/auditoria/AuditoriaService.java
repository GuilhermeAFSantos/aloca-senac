package br.senac.alocasenac.auditoria;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Grava auditoria na transação corrente (ADR-004) e consulta o histórico (RF10). */
@Service
public class AuditoriaService {

    private final RegistroAuditoriaRepository registros;
    private final Clock clock;

    public AuditoriaService(RegistroAuditoriaRepository registros, Clock clock) {
        this.registros = registros;
        this.clock = clock;
    }

    /**
     * Grava o registro na transação de quem chama: se a auditoria falhar, a mudança também é desfeita (I-5).
     * Os serviços que mudam estado sempre chamam este método dentro da própria transação.
     */
    @Transactional
    public void registrar(String entidade, Long entidadeId, String acao, String valorAnterior, String valorNovo,
            String ator) {
        registros.save(RegistroAuditoria.de(entidade, entidadeId, acao, valorAnterior, valorNovo, ator, clock));
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> historico(String entidade, Long entidadeId) {
        return registros.findByEntidadeAndEntidadeIdOrderByInstanteAscIdAsc(entidade, entidadeId);
    }
}
