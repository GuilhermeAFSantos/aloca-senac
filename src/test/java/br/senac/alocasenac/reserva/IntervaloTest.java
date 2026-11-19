package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** RN-T1 — O término deve ser posterior ao início. */
class IntervaloTest {

    @Test
    @DisplayName("RN-T1-C01: término depois do início é aceito")
    void RN_T1_C01_terminoDepoisDoInicioEAceito() {
        // Arrange
        Instant inicio = h("09:00");
        Instant fim = h("10:00");

        // Act
        Intervalo intervalo = Intervalo.de(inicio, fim);

        // Assert
        assertThat(intervalo.inicio()).isEqualTo(inicio);
        assertThat(intervalo.fim()).isEqualTo(fim);
    }

    @Test
    @DisplayName("RN-T1-C02: término igual ao início é recusado")
    void RN_T1_C02_terminoIgualAoInicioERecusado() {
        // Arrange
        Instant instante = h("09:00");

        // Act + Assert
        assertThatThrownBy(() -> Intervalo.de(instante, instante))
                .isInstanceOf(IntervaloInvalidoException.class);
    }

    @Test
    @DisplayName("RN-T1-C04: término antes do início é recusado")
    void RN_T1_C04_terminoAntesDoInicioERecusado() {
        // Arrange
        Instant inicio = h("10:00");
        Instant fim = h("09:00");

        // Act + Assert
        assertThatThrownBy(() -> Intervalo.de(inicio, fim))
                .isInstanceOf(IntervaloInvalidoException.class);
    }

    @ParameterizedTest(name = "RN-T1-C05: início={0}, término={1} é recusado")
    @CsvSource(nullValues = "null", value = { "null, 10:00", "09:00, null", "null, null" })
    void RN_T1_C05_inicioOuTerminoAusenteERecusado(String inicio, String fim) {
        // Arrange
        Instant i = inicio == null ? null : h(inicio);
        Instant f = fim == null ? null : h(fim);

        // Act + Assert
        assertThatThrownBy(() -> Intervalo.de(i, f))
                .isInstanceOf(IntervaloInvalidoException.class);
    }
}
