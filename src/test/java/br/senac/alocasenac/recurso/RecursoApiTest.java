package br.senac.alocasenac.recurso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.senac.alocasenac.IntegracaoBase;

/** RF1 — cadastro de recursos é do Administrador (docs/requisitos.md §2). */
class RecursoApiTest extends IntegracaoBase {

    private static final String SALA_JSON = """
            {"nome": "S-901", "tipo": "SALA", "capacidade": 30, "localizacao": "Bloco J", "restrito": false}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("RF1-C03: Solicitante não cadastra sala")
    void RF1_C03_solicitanteNaoCadastraSala() throws Exception {
        // Act + Assert
        mvc.perform(post("/api/recursos").with(csrf()).with(user("mariana.teste@example.invalid").roles("SOLICITANTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(SALA_JSON))
                .andExpect(status().isForbidden());
        assertThat(recursos.count()).isZero();
    }

    @Test
    @DisplayName("RF1-C04: Administrador cadastra sala")
    void RF1_C04_administradorCadastraSala() throws Exception {
        // Act + Assert
        mvc.perform(post("/api/recursos").with(csrf()).with(user("roberto.teste@example.invalid").roles("ADMINISTRADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(SALA_JSON))
                .andExpect(status().isCreated());
        assertThat(recursos.count()).isEqualTo(1);
    }
}
