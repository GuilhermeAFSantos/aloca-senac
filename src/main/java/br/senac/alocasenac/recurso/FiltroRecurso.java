package br.senac.alocasenac.recurso;

import br.senac.alocasenac.reserva.Intervalo;

/** Critérios de pesquisa de RF3; campos nulos não filtram. */
public record FiltroRecurso(TipoRecurso tipo, Integer capacidadeMinima, String localizacao, Intervalo disponivelEm) {
}
