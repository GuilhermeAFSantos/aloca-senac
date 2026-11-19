package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/**
 * RF1, RN-T1 e RN-E4 pela API (caixa-preta).
 * O usuário autenticado é simulado pelo spring-security-test, sem depender do mecanismo
 * de autenticação (decidido na Aula 06).
 */
class ReservaApiTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";

    @Autowired
    private MockMvc mvc;

    private Long sala() {
        return recursos.save(new Recurso("S-301", TipoRecurso.SALA, 30, "Bloco C", false)).getId();
    }

    private static String corpoReserva(Long recursoId, String inicio, String fim) {
        return """
                {"recursoIds": [%d], "inicio": "%s", "fim": "%s"}
                """.formatted(recursoId, h(inicio), h(fim));
    }

    private static String corpoAlteracao(String inicio, String fim) {
        return """
                {"inicio": "%s", "fim": "%s"}
                """.formatted(h(inicio), h(fim));
    }

    @Test
    @DisplayName("RF1-C01: usuário anônimo não acessa a criação de reserva")
    void RF1_C01_anonimoNaoCriaReserva() throws Exception {
        // Arrange
        Long salaId = sala();

        // Act + Assert
        mvc.perform(post("/api/reservas").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoReserva(salaId, "09:00", "10:00")))
                .andExpect(status().isUnauthorized());
        assertThat(reservas.count()).isZero();
    }

    @Test
    @DisplayName("RF1-C02: Solicitante cria reserva")
    void RF1_C02_solicitanteCriaReserva() throws Exception {
        // Arrange
        Long salaId = sala();

        // Act + Assert
        mvc.perform(post("/api/reservas").with(csrf()).with(user(MARIANA).roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoReserva(salaId, "09:00", "10:00")))
                .andExpect(status().isCreated());
        assertThat(reservas.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("RF1-C05: Solicitante não altera reserva de outro solicitante (D-03)")
    void RF1_C05_solicitanteNaoAlteraReservaDeOutro() throws Exception {
        // Arrange
        Long salaId = sala();
        Reserva daMariana = reservas.save(new Reserva(MARIANA, Set.of(salaId), h("09:00"), h("10:00"),
                EstadoReserva.SOLICITADA));

        // Act + Assert
        mvc.perform(put("/api/reservas/{id}", daMariana.getId()).with(csrf())
                .with(user("carla.teste@example.invalid").roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoAlteracao("11:00", "12:00")))
                .andExpect(status().isForbidden());
        assertThat(reservas.findById(daMariana.getId()).orElseThrow().intervalo().inicio()).isEqualTo(h("09:00"));
    }

    @Test
    @DisplayName("RF1-C06: Responsável altera reserva de um solicitante (D-03)")
    void RF1_C06_responsavelAlteraReservaDeSolicitante() throws Exception {
        // Arrange
        Long salaId = sala();
        Reserva daMariana = reservas.save(new Reserva(MARIANA, Set.of(salaId), h("09:00"), h("10:00"),
                EstadoReserva.SOLICITADA));
        ocupacoes.save(new Ocupacao(salaId, daMariana.getId(), h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true));

        // Act + Assert
        mvc.perform(put("/api/reservas/{id}", daMariana.getId()).with(csrf())
                .with(user("juliana.teste@example.invalid").roles("RESPONSAVEL"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoAlteracao("11:00", "12:00")))
                .andExpect(status().isOk());
        assertThat(reservas.findById(daMariana.getId()).orElseThrow().intervalo().inicio()).isEqualTo(h("11:00"));
    }

    @Test
    @DisplayName("RN-T1-C06: criar reserva com término antes do início é recusado com 400 e nada é gravado")
    void RN_T1_C06_intervaloInvalidoPelaApiERecusado() throws Exception {
        // Arrange
        Long salaId = sala();

        // Act + Assert
        mvc.perform(post("/api/reservas").with(csrf()).with(user(MARIANA).roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoReserva(salaId, "10:00", "09:00")))
                .andExpect(status().isBadRequest());
        assertThat(reservas.count()).isZero();
    }

    @Test
    @DisplayName("RN-E4-C04: apagar reserva iniciada pela API é recusado com 409 e a reserva continua existindo")
    void RN_E4_C04_apagarReservaIniciadaPelaApiERecusado() throws Exception {
        // Arrange
        Long salaId = sala();
        Reserva emUso = reservas.save(new Reserva(MARIANA, Set.of(salaId), h("09:00"), h("10:00"),
                EstadoReserva.EM_USO));

        // Act + Assert
        mvc.perform(delete("/api/reservas/{id}", emUso.getId()).with(csrf())
                .with(user("roberto.teste@example.invalid").roles("ADMINISTRADOR")))
                .andExpect(status().isConflict());
        assertThat(reservas.existsById(emUso.getId())).isTrue();
    }
}
