package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** B3 — bloqueio de manutenção (RF8) e regra de tempo D-12, com o "agora" injetado. */
class BloqueioTest {

    private static final long SALA_S101 = 101L;

    @ParameterizedTest(name = "B3-12: agora 08:00, bloqueio {0}–10:00 → {1}")
    @CsvSource({ "07:59:59, recusado", "08:00, aceito", "08:00:01, aceito" })
    void B3_12_bloqueioNaoComecaNoPassado(String inicio, String resultado) {
        // Arrange
        Instant agora = h("08:00");
        Intervalo periodo = new Intervalo(h(inicio), h("10:00"));

        // Act + Assert
        if (resultado.equals("aceito")) {
            Ocupacao bloqueio = Ocupacao.bloqueio(SALA_S101, periodo, agora);
            assertThat(bloqueio.getOrigem()).isEqualTo(OrigemOcupacao.BLOQUEIO);
            assertThat(bloqueio.intervalo()).isEqualTo(periodo);
        } else {
            assertThatThrownBy(() -> Ocupacao.bloqueio(SALA_S101, periodo, agora))
                    .isInstanceOf(IntervaloInvalidoException.class);
        }
    }
}
