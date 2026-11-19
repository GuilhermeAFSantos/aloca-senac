package br.senac.alocasenac.reserva;

import java.time.Instant;

public record AlterarReservaRequest(Instant inicio, Instant fim) {
}
