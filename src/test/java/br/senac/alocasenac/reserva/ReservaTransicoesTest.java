package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** B4 — a reserva só muda de estado pelas transições permitidas (RN-E1, RN-E2). */
class ReservaTransicoesTest {

    private static final Instant INICIO = h("09:00");

    private static Reserva reservaNoEstado(EstadoReserva estado) {
        return new Reserva("mariana.teste@example.invalid", Set.of(1L), INICIO, h("10:00"), estado);
    }

    @ParameterizedTest(name = "B4-01: {0} + {1} → {2}")
    @CsvSource({
        "SOLICITADA, APROVAR,  APROVADA",
        "APROVADA,   INICIAR,  EM_USO",
        "EM_USO,     CONCLUIR, CONCLUIDA"
    })
    void B4_01_transicoesDoFluxoPrincipalSaoPermitidas(EstadoReserva origem, AcaoReserva acao,
            EstadoReserva destino) {
        // Arrange
        Reserva reserva = reservaNoEstado(origem);

        // Act
        reserva.aplicar(acao, INICIO);

        // Assert
        assertThat(reserva.getEstado()).isEqualTo(destino);
    }

    @ParameterizedTest(name = "B4-02: {0} + {1} → {2}")
    @CsvSource({
        "SOLICITADA, REJEITAR,              REJEITADA",
        "SOLICITADA, CANCELAR,              CANCELADA",
        "APROVADA,   CANCELAR,              CANCELADA",
        "APROVADA,   MARCAR_NAO_COMPARECEU, NAO_COMPARECEU"
    })
    void B4_02_transicoesAlternativasSaoPermitidas(EstadoReserva origem, AcaoReserva acao, EstadoReserva destino) {
        // Arrange
        Reserva reserva = reservaNoEstado(origem);

        // Act
        reserva.aplicar(acao, INICIO);

        // Assert
        assertThat(reserva.getEstado()).isEqualTo(destino);
    }

    @ParameterizedTest(name = "B4-03: {0} + {1} é recusada")
    @CsvSource({
        "SOLICITADA,     INICIAR",
        "SOLICITADA,     CONCLUIR",
        "SOLICITADA,     MARCAR_NAO_COMPARECEU",
        "APROVADA,       APROVAR",
        "APROVADA,       REJEITAR",
        "APROVADA,       CONCLUIR",
        "EM_USO,         APROVAR",
        "EM_USO,         INICIAR",
        "EM_USO,         CANCELAR",
        "EM_USO,         MARCAR_NAO_COMPARECEU",
        "CONCLUIDA,      CANCELAR",
        "CONCLUIDA,      INICIAR",
        "REJEITADA,      APROVAR",
        "CANCELADA,      INICIAR",
        "NAO_COMPARECEU, INICIAR"
    })
    void B4_03_transicoesForaDoFluxoSaoRecusadas(EstadoReserva origem, AcaoReserva acao) {
        // Arrange
        Reserva reserva = reservaNoEstado(origem);

        // Act + Assert
        assertThatThrownBy(() -> reserva.aplicar(acao, INICIO)).isInstanceOf(TransicaoInvalidaException.class);
        assertThat(reserva.getEstado()).isEqualTo(origem);
    }
}
