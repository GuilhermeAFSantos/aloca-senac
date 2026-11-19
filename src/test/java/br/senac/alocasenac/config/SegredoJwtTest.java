package br.senac.alocasenac.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import br.senac.alocasenac.TokensDeTeste;

/**
 * RF1-C09 — o segredo de assinatura dos tokens precisa ter no mínimo 32 caracteres (ADR-014).
 * Limite em trinca: 31 (−1), 32 (0), 33 (+1). Lacuna de risco G3 da Aula 07 (SegurancaConfig.java:72-73).
 */
class SegredoJwtTest {

    private static final String USUARIO = "roberto.teste@example.invalid";

    @ParameterizedTest(name = "RF1-C09: segredo com {0} caracteres aceita o próprio token? {1}")
    @CsvSource({ "31, false", "32, true", "33, true" })
    void RF1_C09_segredoPrecisaDeNoMinimo32Caracteres(int tamanho, boolean aceito) {
        // Arrange
        String segredo = "s".repeat(tamanho);
        JwtDecoder decodificador = new SegurancaConfig().jwtDecoder(segredo);
        String token = TokensDeTeste.assinar(segredo, USUARIO, List.of("ADMINISTRADOR"));

        // Act + Assert
        if (aceito) {
            assertThat(decodificador.decode(token).getSubject()).isEqualTo(USUARIO);
        } else {
            assertThatThrownBy(() -> decodificador.decode(token)).isInstanceOf(JwtException.class);
        }
    }
}
