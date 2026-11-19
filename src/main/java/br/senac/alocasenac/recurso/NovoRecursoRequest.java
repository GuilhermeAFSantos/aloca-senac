package br.senac.alocasenac.recurso;

public record NovoRecursoRequest(String nome, TipoRecurso tipo, Integer capacidade, String localizacao,
        boolean restrito) {
}
