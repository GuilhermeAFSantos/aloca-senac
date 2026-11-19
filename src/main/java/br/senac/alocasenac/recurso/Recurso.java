package br.senac.alocasenac.recurso;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Sala, professor ou material reservável (RF2). */
@Entity
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private TipoRecurso tipo;

    private Integer capacidade;

    private String localizacao;

    private boolean restrito;

    protected Recurso() {
    }

    /** Construtor de reconstituição (persistência e fixtures de teste); não aplica regras. */
    public Recurso(String nome, TipoRecurso tipo, Integer capacidade, String localizacao, boolean restrito) {
        this.nome = nome;
        this.tipo = tipo;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.restrito = restrito;
    }

    /** Cria um recurso validando os dados obrigatórios (RF2). */
    public static Recurso novo(String nome, TipoRecurso tipo, Integer capacidade, String localizacao,
            boolean restrito) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("Informe o nome do recurso.");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("Informe o tipo do recurso: sala, professor ou material.");
        }
        return new Recurso(nome.strip(), tipo, capacidade, localizacao, restrito);
    }

    /** Resumo usado na auditoria (RF10). */
    public String descricao() {
        return "tipo=" + tipo + "; nome=" + nome + "; capacidade=" + capacidade + "; localizacao=" + localizacao
                + "; restrito=" + restrito;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public boolean isRestrito() {
        return restrito;
    }
}
