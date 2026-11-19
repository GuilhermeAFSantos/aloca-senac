package br.senac.alocasenac.reserva;

import java.time.Instant;
import java.util.Set;

public record CriarReservaRequest(Set<Long> recursoIds, Instant inicio, Instant fim) {
}
