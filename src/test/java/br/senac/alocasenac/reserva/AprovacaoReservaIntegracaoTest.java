package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/** B1 — recurso restrito exige aprovação de um Responsável (RF7, RN-E3); D-05 e D-09. */
class AprovacaoReservaIntegracaoTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";

    @Autowired
    private ReservaService reservaService;

    private Long salaS101() {
        return recursos.save(new Recurso("Sala S-101", TipoRecurso.SALA, 30, "Bloco A", false)).getId();
    }

    @Test
    @DisplayName("B1-08: reserva só de recursos não restritos é aprovada pelo sistema")
    void B1_08_reservaSoDeRecursosNaoRestritosEAprovadaPeloSistema() {
        // Arrange
        Long sala = salaS101();

        // Act
        Reserva reserva = reservaService.criar(MARIANA, Set.of(sala), h("09:00"), h("10:00"));

        // Assert
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.APROVADA);
    }
}
