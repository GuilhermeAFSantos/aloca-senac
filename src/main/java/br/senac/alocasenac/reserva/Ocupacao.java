package br.senac.alocasenac.reserva;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Ocupação de um recurso por reserva ou bloqueio (ADR-003). */
@Entity
public class Ocupacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recursoId;

    private Long referenciaId;

    private Instant inicio;

    private Instant fim;

    @Enumerated(EnumType.STRING)
    private OrigemOcupacao origem;

    private boolean ativa;

    protected Ocupacao() {
    }

    public Ocupacao(Long recursoId, Long referenciaId, Instant inicio, Instant fim, OrigemOcupacao origem,
            boolean ativa) {
        this.recursoId = recursoId;
        this.referenciaId = referenciaId;
        this.inicio = inicio;
        this.fim = fim;
        this.origem = origem;
        this.ativa = ativa;
    }

    public Long getId() {
        return id;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public Intervalo intervalo() {
        return new Intervalo(inicio, fim);
    }

    /** Indica se a ocupação foi criada pela reserva informada (nula nunca pertence). */
    public boolean pertenceAReserva(Long reservaId) {
        return reservaId != null && origem == OrigemOcupacao.RESERVA && reservaId.equals(referenciaId);
    }

    /** Move a ocupação para outro intervalo, acompanhando a alteração da reserva (RF4). */
    public void reagendar(Intervalo novo) {
        this.inicio = novo.inicio();
        this.fim = novo.fim();
    }

    public OrigemOcupacao getOrigem() {
        return origem;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
