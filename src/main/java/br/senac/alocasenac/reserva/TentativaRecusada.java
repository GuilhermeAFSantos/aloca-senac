package br.senac.alocasenac.reserva;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Tentativa de reserva recusada, base de "conflitos evitados" (RF12; ADR-004). */
@Entity
public class TentativaRecusada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String solicitante;

    private Long recursoId;

    private Instant inicio;

    private Instant fim;

    @Enumerated(EnumType.STRING)
    private MotivoRecusa motivo;

    private Instant instante;

    protected TentativaRecusada() {
    }

    public TentativaRecusada(String solicitante, Long recursoId, Instant inicio, Instant fim, MotivoRecusa motivo,
            Instant instante) {
        this.solicitante = solicitante;
        this.recursoId = recursoId;
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
        this.instante = instante;
    }

    public Long getId() {
        return id;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public Intervalo intervalo() {
        return new Intervalo(inicio, fim);
    }

    public MotivoRecusa getMotivo() {
        return motivo;
    }

    public Instant getInstante() {
        return instante;
    }
}
