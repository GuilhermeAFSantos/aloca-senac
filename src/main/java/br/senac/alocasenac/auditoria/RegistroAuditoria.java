package br.senac.alocasenac.auditoria;

import java.time.Clock;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Registro de uma mudança: quem, o quê, quando (RF10, RN-E5; ADR-004). */
@Entity
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidade;

    private Long entidadeId;

    private String acao;

    private String valorAnterior;

    private String valorNovo;

    private String ator;

    private Instant instante;

    protected RegistroAuditoria() {
    }

    /** Construtor de reconstituição (persistência e fixtures de teste); não aplica regras. */
    public RegistroAuditoria(String entidade, Long entidadeId, String acao, String valorAnterior, String valorNovo,
            String ator, Instant instante) {
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.acao = acao;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
        this.ator = ator;
        this.instante = instante;
    }

    /** Cria o registro com o instante dado pelo relógio injetado. */
    public static RegistroAuditoria de(String entidade, Long entidadeId, String acao, String valorAnterior,
            String valorNovo, String ator, Clock clock) {
        return new RegistroAuditoria();
    }

    public Long getId() {
        return id;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getAcao() {
        return acao;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public String getAtor() {
        return ator;
    }

    public Instant getInstante() {
        return instante;
    }
}
