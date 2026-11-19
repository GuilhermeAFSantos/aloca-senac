package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/**
 * RF13 (mensagens de erro compreensíveis) e RQ-6 (tratamento seguro de erros) pela API.
 * Lacuna de risco G7 da Aula 07 (TratamentoDeErros.java:31, 36-37, 47).
 */
class ReservaApiErrosTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";

    @Autowired
    private MockMvc mvc;

    private static String corpo(Long recursoId, String inicio, String fim) {
        return """
                {"recursoIds": [%d], "inicio": "%s", "fim": "%s"}
                """.formatted(recursoId, h(inicio), h(fim));
    }

    @Test
    @DisplayName("RF13-C01: reservar horário ocupado pela API devolve 409 com mensagem compreensível")
    void RF13_C01_horarioOcupadoDevolve409ComMensagemCompreensivel() throws Exception {
        // Arrange
        Long salaId = recursos.save(new Recurso("S-970", TipoRecurso.SALA, 30, "Bloco M", false)).getId();
        ocupacoes.save(new Ocupacao(salaId, 1L, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true));

        // Act + Assert
        mvc.perform(post("/api/reservas").with(user(MARIANA).roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(salaId, "09:30", "10:30")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Horário indisponível"))
                .andExpect(jsonPath("$.detail").value(containsString("Escolha outro horário")));
    }

    @Test
    @DisplayName("RF13-C02: corpo ilegível devolve 400 sem detalhes internos")
    void RF13_C02_corpoIlegivelDevolve400SemDetalhesInternos() throws Exception {
        // Act + Assert
        mvc.perform(post("/api/reservas").with(user(MARIANA).roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ isto não é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O corpo da requisição não pôde ser lido."))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    @DisplayName("RF13-C03: alterar reserva inexistente devolve 404")
    void RF13_C03_reservaInexistenteDevolve404() throws Exception {
        // Act + Assert
        mvc.perform(put("/api/reservas/{id}", 999_999L).with(user(MARIANA).roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"inicio": "%s", "fim": "%s"}
                        """.formatted(h("09:00"), h("10:00"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Não encontrado"));
    }
}
