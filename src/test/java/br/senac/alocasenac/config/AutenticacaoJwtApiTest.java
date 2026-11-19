package br.senac.alocasenac.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.TokensDeTeste;
import br.senac.alocasenac.auditoria.RegistroAuditoria;

/**
 * RF1 com token JWT real, passando pelo decodificador e pelo conversor de perfis (ADR-014).
 * Lacuna de risco G3 da Aula 07 e risco R1-01 do ATAM ciclo 01: até aqui os testes simulavam o usuário.
 */
class AutenticacaoJwtApiTest extends IntegracaoBase {

    /** Segredo sintético, só deste teste; o real vem de ALOCASENAC_JWT_SEGREDO. */
    private static final String SEGREDO = "t".repeat(48);
    private static final String ROBERTO = "roberto.teste@example.invalid";
    private static final String SALA_JSON = """
            {"nome": "S-950", "tipo": "SALA", "capacidade": 30, "localizacao": "Bloco J", "restrito": false}
            """;

    @DynamicPropertySource
    static void segredoDeTeste(DynamicPropertyRegistry registro) {
        registro.add("alocasenac.jwt.segredo", () -> SEGREDO);
    }

    @Autowired
    private MockMvc mvc;

    private org.springframework.test.web.servlet.ResultActions cadastrarSalaCom(String token) throws Exception {
        return mvc.perform(post("/api/recursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(SALA_JSON));
    }

    @Test
    @DisplayName("RF1-C10: token de Administrador cadastra sala e o sub do token vira o ator da auditoria")
    void RF1_C10_tokenDeAdministradorCadastraSala() throws Exception {
        // Arrange
        String token = TokensDeTeste.assinar(SEGREDO, ROBERTO, List.of("ADMINISTRADOR"));

        // Act + Assert
        cadastrarSalaCom(token).andExpect(status().isCreated());
        assertThat(recursos.count()).isEqualTo(1);
        assertThat(auditoria.findAll()).extracting(RegistroAuditoria::getAtor).containsExactly(ROBERTO);
    }

    @Test
    @DisplayName("RF1-C11: token de Solicitante não cadastra sala")
    void RF1_C11_tokenDeSolicitanteNaoCadastraSala() throws Exception {
        // Arrange
        String token = TokensDeTeste.assinar(SEGREDO, "mariana.teste@example.invalid", List.of("SOLICITANTE"));

        // Act + Assert
        cadastrarSalaCom(token).andExpect(status().isForbidden());
        assertThat(recursos.count()).isZero();
    }

    @Test
    @DisplayName("RF1-C12: token sem perfil não cadastra sala")
    void RF1_C12_tokenSemPerfilNaoCadastraSala() throws Exception {
        // Arrange
        String token = TokensDeTeste.assinar(SEGREDO, ROBERTO, List.of());

        // Act + Assert
        cadastrarSalaCom(token).andExpect(status().isForbidden());
        assertThat(recursos.count()).isZero();
    }

    @Test
    @DisplayName("RF1-C13: token assinado com outro segredo é recusado como não autenticado")
    void RF1_C13_tokenComAssinaturaInvalidaERecusado() throws Exception {
        // Arrange
        String token = TokensDeTeste.assinar("x".repeat(48), ROBERTO, List.of("ADMINISTRADOR"));

        // Act + Assert
        cadastrarSalaCom(token).andExpect(status().isUnauthorized());
        assertThat(recursos.count()).isZero();
    }
}
