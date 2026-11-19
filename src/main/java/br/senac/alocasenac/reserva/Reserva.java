package br.senac.alocasenac.reserva;

import java.time.Instant;
import java.util.HashSet;
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
        return new Reserva();
    }

    /** RN-E4 e decisão D-04: verifica se a reserva pode ser apagada. */
    public void verificarPodeSerApagada() {
        // assinatura mínima: regra implementada na Aula 06
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
