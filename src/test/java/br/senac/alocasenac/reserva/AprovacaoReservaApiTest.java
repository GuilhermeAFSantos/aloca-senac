package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/** B1 e B2 pela API — aprovação de recurso restrito (RF7, RN-E3) e ninguém aprova a própria reserva. */
class AprovacaoReservaApiTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";
    private static final String JULIANA = "juliana.teste@example.invalid";

    @Autowired
    private MockMvc mvc;

    /** Dado que {@code solicitante} tem uma reserva SOLICITADA do Laboratório L-01 das 09:00 às 10:00. */
    private Reserva reservaSolicitadaDoLaboratorio(String solicitante) {
        Long laboratorio = recursos.save(new Recurso("Laboratório L-01", TipoRecurso.SALA, 20, "Bloco B", true))
                .getId();
        Reserva reserva = reservas.save(new Reserva(solicitante, Set.of(laboratorio), h("09:00"), h("10:00"),
                EstadoReserva.SOLICITADA));
        ocupacoes.save(new Ocupacao(laboratorio, reserva.getId(), h("09:00"), h("10:00"), OrigemOcupacao.RESERVA,
                true));
        return reserva;
    }

    private EstadoReserva estadoDe(Reserva reserva) {
        return reservas.findById(reserva.getId()).orElseThrow().getEstado();
    }

    @Test
    @DisplayName("B1-02: Responsável aprova reserva de recurso restrito")
    void B1_02_responsavelAprovaReservaDeRecursoRestrito() throws Exception {
        // Arrange
        Reserva reserva = reservaSolicitadaDoLaboratorio(MARIANA);

        // Act
        mvc.perform(post("/api/reservas/{id}/aprovacao", reserva.getId())
                .with(user(JULIANA).roles("RESPONSAVEL")))
                .andExpect(status().isOk());

        // Assert
        assertThat(estadoDe(reserva)).isEqualTo(EstadoReserva.APROVADA);
    }
}
