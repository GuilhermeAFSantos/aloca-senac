package br.senac.alocasenac.reserva;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Decisão D-03: Solicitante age só sobre as próprias reservas; Responsável e Administrador, sobre qualquer uma. */
@Component
public class PoliticaAcessoReserva {

    public void exigirPermissaoParaAlterar(Reserva reserva, Authentication usuario) {
        if (temPerfil(usuario, "ROLE_RESPONSAVEL") || temPerfil(usuario, "ROLE_ADMINISTRADOR")) {
            return;
        }
        if (!reserva.getSolicitante().equals(usuario.getName())) {
            throw new AccessDeniedException("Você só pode alterar as suas próprias reservas.");
        }
    }

    private static boolean temPerfil(Authentication usuario, String perfil) {
        return usuario.getAuthorities().stream().anyMatch(a -> perfil.equals(a.getAuthority()));
    }
}
