package br.senac.alocasenac.reserva;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservas;
    private final PoliticaAcessoReserva politica;

    public ReservaController(ReservaService reservas, PoliticaAcessoReserva politica) {
        this.reservas = reservas;
        this.politica = politica;
    }

    /** Resposta da API: dados públicos da reserva. */
    public record ReservaResponse(Long id, String solicitante, Set<Long> recursoIds, Instant inicio, Instant fim,
            EstadoReserva estado) {

        static ReservaResponse de(Reserva reserva) {
            return new ReservaResponse(reserva.getId(), reserva.getSolicitante(), reserva.getRecursoIds(),
                    reserva.intervalo().inicio(), reserva.intervalo().fim(), reserva.getEstado());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SOLICITANTE')")
    public ResponseEntity<ReservaResponse> criar(@RequestBody CriarReservaRequest pedido, Authentication usuario) {
        Reserva reserva = reservas.criar(usuario.getName(), pedido.recursoIds(), pedido.inicio(), pedido.fim());
        return ResponseEntity.created(URI.create("/api/reservas/" + reserva.getId())).body(ReservaResponse.de(reserva));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservaResponse> alterar(@PathVariable Long id, @RequestBody AlterarReservaRequest pedido,
            Authentication usuario) {
        politica.exigirPermissaoParaAlterar(reservas.buscar(id), usuario);
        Reserva alterada = reservas.alterar(id, pedido.inicio(), pedido.fim(), usuario.getName());
        return ResponseEntity.ok(ReservaResponse.de(alterada));
    }

    @PostMapping("/{id}/aprovacao")
    public ResponseEntity<ReservaResponse> aprovar(@PathVariable Long id, Authentication usuario) {
        return ResponseEntity.ok(ReservaResponse.de(reservas.aprovar(id, usuario.getName())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> apagar(@PathVariable Long id, Authentication usuario) {
        reservas.apagar(id, usuario.getName());
        return ResponseEntity.noContent().build();
    }
}
