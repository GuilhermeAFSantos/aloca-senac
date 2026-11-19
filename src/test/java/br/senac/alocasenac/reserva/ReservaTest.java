package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** RN-E1 (estado inicial) e RN-E4 com a decisão D-04 (nenhuma reserva é apagada). */
class ReservaTest {

    @Test
    @DisplayName("RN-E1-C01: nova reserva nasce SOLICITADA")
    void RN_E1_C01_novaReservaNasceSolicitada() {
        // Arrange
        Intervalo intervalo = new Intervalo(h("09:00"), h("10:00"));

        // Act
        Reserva reserva = Reserva.nova("mariana.teste@example.invalid", Set.of(1L), intervalo);

        // Assert
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.SOLICITADA);
        assertThat(reserva.getSolicitante()).isEqualTo("mariana.teste@example.invalid");
        assertThat(reserva.getRecursoIds()).containsExactly(1L);
        assertThat(reserva.intervalo()).isEqualTo(intervalo);
    }

    @ParameterizedTest(name = "RN-E4-C01/C02: reserva {0} (iniciada) não pode ser apagada")
    @EnumSource(value = EstadoReserva.class, names = { "EM_USO", "CONCLUIDA" })
    void RN_E4_C01eC02_reservaIniciadaNaoPodeSerApagada(EstadoReserva estado) {
        // Arrange
        Reserva reserva = new Reserva("mariana.teste@example.invalid", Set.of(1L), h("09:00"), h("10:00"), estado);

        // Act + Assert
        assertThatThrownBy(reserva::verificarPodeSerApagada).isInstanceOf(OperacaoProibidaException.class);
    }

    @ParameterizedTest(name = "RN-E4-C03: reserva {0} (não iniciada) também não é apagada, só cancelada (D-04)")
    @EnumSource(value = EstadoReserva.class, names = { "SOLICITADA", "APROVADA" })
    void RN_E4_C03_reservaNaoIniciadaTambemNaoEApagada(EstadoReserva estado) {
        // Arrange
        Reserva reserva = new Reserva("mariana.teste@example.invalid", Set.of(1L), h("09:00"), h("10:00"), estado);

        // Act + Assert
        assertThatThrownBy(reserva::verificarPodeSerApagada).isInstanceOf(OperacaoProibidaException.class);
    }
}
