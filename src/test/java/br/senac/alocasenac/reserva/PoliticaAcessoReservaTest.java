package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * RF1 com a decisão D-03: Solicitante altera só as próprias reservas; Responsável e Administrador, qualquer uma.
 * Lacuna de risco G5 da Aula 07 (PoliticaAcessoReserva.java:12,15).
 */
class PoliticaAcessoReservaTest {

    private static final String MARIANA = "mariana.teste@example.invalid";

    private final PoliticaAcessoReserva politica = new PoliticaAcessoReserva();
    private final Reserva daMariana = new Reserva(MARIANA, Set.of(1L), h("09:00"), h("10:00"),
            EstadoReserva.SOLICITADA);

    private static Authentication usuario(String nome, String perfil) {
        return new UsernamePasswordAuthenticationToken(nome, null, AuthorityUtils.createAuthorityList("ROLE_" + perfil));
    }

    @ParameterizedTest(name = "RF1-C07/C08: {1} ({0}) pode alterar a reserva da Mariana")
    @CsvSource({
        "mariana.teste@example.invalid, SOLICITANTE",
        "juliana.teste@example.invalid, RESPONSAVEL",
        "roberto.teste@example.invalid, ADMINISTRADOR"
    })
    void RF1_C07eC08_donoResponsavelEAdministradorPodemAlterar(String nome, String perfil) {
        // Act + Assert
        assertThatCode(() -> politica.exigirPermissaoParaAlterar(daMariana, usuario(nome, perfil)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "RF1-C05: {1} ({0}) que não é dono não pode alterar")
    @CsvSource({ "carla.teste@example.invalid, SOLICITANTE" })
    void RF1_C05_solicitanteQueNaoEDonoNaoPodeAlterar(String nome, String perfil) {
        // Act + Assert
        assertThatThrownBy(() -> politica.exigirPermissaoParaAlterar(daMariana, usuario(nome, perfil)))
                .isInstanceOf(AccessDeniedException.class);
    }
}
