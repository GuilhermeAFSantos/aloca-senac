package br.senac.alocasenac.emprestimo;

import java.time.Clock;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Retirada e devolução de um material ou equipamento de uma reserva (RF9). */
@Entity
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservaId;

    private Long recursoId;

    private Instant retiradaEm;

    private String retiradoPor;

    private Instant devolvidoEm;

    private String devolvidoPor;

    protected Emprestimo() {
    }

    public Emprestimo(Long reservaId, Long recursoId) {
        this.reservaId = reservaId;
        this.recursoId = recursoId;
    }

    public void retirar(String ator, Clock clock) {
        // assinatura mínima: regra implementada na Aula 06
    }

    public void devolver(String ator, Clock clock) {
        // assinatura mínima: regra implementada na Aula 06
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public Instant getRetiradaEm() {
        return retiradaEm;
    }

    public String getRetiradoPor() {
        return retiradoPor;
    }

    public Instant getDevolvidoEm() {
        return devolvidoEm;
    }

    public String getDevolvidoPor() {
        return devolvidoPor;
    }
}
