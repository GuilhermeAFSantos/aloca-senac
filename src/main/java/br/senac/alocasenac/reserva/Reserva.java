package br.senac.alocasenac.reserva;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Reserva de um ou mais recursos em um intervalo (RF4). */
@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String solicitante;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> recursoIds = new HashSet<>();

    private Instant inicio;

    private Instant fim;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    protected Reserva() {
    }

    /** Construtor de reconstituição (persistência e fixtures de teste); não aplica regras. */
    public Reserva(String solicitante, Set<Long> recursoIds, Instant inicio, Instant fim, EstadoReserva estado) {
        this.solicitante = solicitante;
        this.recursoIds = new HashSet<>(recursoIds);
        this.inicio = inicio;
        this.fim = fim;
        this.estado = estado;
    }

    /** Nova reserva pedida por um solicitante (RN-E1: estado inicial). */
    public static Reserva nova(String solicitante, Set<Long> recursoIds, Intervalo intervalo) {
        return new Reserva(solicitante, recursoIds, intervalo.inicio(), intervalo.fim(), EstadoReserva.SOLICITADA);
    }

    /**
     * RN-E4 e decisão D-04: nenhuma reserva é apagada. Reservas iniciadas preservam o histórico (RN-E4);
     * as não iniciadas devem ser canceladas, não apagadas (D-04).
     */
    public void verificarPodeSerApagada() {
        boolean iniciada = estado == EstadoReserva.EM_USO || estado == EstadoReserva.CONCLUIDA;
        throw new OperacaoProibidaException(iniciada
                ? "Reservas iniciadas não podem ser apagadas."
                : "Reservas não são apagadas; para desistir, cancele a reserva.");
    }

    /** Aplica uma ação de mudança de estado (RN-E1, RN-E2), no instante {@code agora}. */
    public void aplicar(AcaoReserva acao, Instant agora) {
        EstadoReserva destino = TRANSICOES.getOrDefault(estado, Map.of()).get(acao);
        if (destino == null) {
            throw new TransicaoInvalidaException(
                    "Não é possível " + acao.name().toLowerCase().replace('_', ' ') + " uma reserva " + estado + ".");
        }
        if (acao == AcaoReserva.INICIAR && agora.isBefore(inicio)) {
            throw new TransicaoInvalidaException("O uso só pode começar a partir do horário de início da reserva.");
        }
        if (acao == AcaoReserva.MARCAR_NAO_COMPARECEU && agora.isBefore(inicio)) {
            throw new TransicaoInvalidaException("Só é possível marcar não compareceu a partir do horário de início.");
        }
        this.estado = destino;
    }

    /** Transições permitidas: origem → (ação → destino). */
    private static final Map<EstadoReserva, Map<AcaoReserva, EstadoReserva>> TRANSICOES = Map.of(
            EstadoReserva.SOLICITADA, Map.of(
                    AcaoReserva.APROVAR, EstadoReserva.APROVADA,
                    AcaoReserva.REJEITAR, EstadoReserva.REJEITADA,
                    AcaoReserva.CANCELAR, EstadoReserva.CANCELADA),
            EstadoReserva.APROVADA, Map.of(
                    AcaoReserva.INICIAR, EstadoReserva.EM_USO,
                    AcaoReserva.CANCELAR, EstadoReserva.CANCELADA,
                    AcaoReserva.MARCAR_NAO_COMPARECEU, EstadoReserva.NAO_COMPARECEU),
            EstadoReserva.EM_USO, Map.of(AcaoReserva.CONCLUIR, EstadoReserva.CONCLUIDA));

    /** Move a reserva para outro intervalo (RF4); a disponibilidade é verificada pelo serviço. */
    public void reagendar(Intervalo novo) {
        this.inicio = novo.inicio();
        this.fim = novo.fim();
    }

    /** Resumo usado na auditoria (RN-E5). */
    public String descricao() {
        return "estado=" + estado + "; " + intervalo();
    }

    public Long getId() {
        return id;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public Set<Long> getRecursoIds() {
        return Set.copyOf(recursoIds);
    }

    public Intervalo intervalo() {
        return new Intervalo(inicio, fim);
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}
