package br.senac.alocasenac.recurso;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    private final RecursoService recursos;

    public RecursoController(RecursoService recursos) {
        this.recursos = recursos;
    }

    /** Resposta da API: dados públicos do recurso. */
    public record RecursoResponse(Long id, String nome, TipoRecurso tipo, Integer capacidade, String localizacao,
            boolean restrito) {

        static RecursoResponse de(Recurso r) {
            return new RecursoResponse(r.getId(), r.getNome(), r.getTipo(), r.getCapacidade(), r.getLocalizacao(),
                    r.isRestrito());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RecursoResponse> cadastrar(@RequestBody NovoRecursoRequest dados, Authentication usuario) {
        Recurso recurso = recursos.cadastrar(dados, usuario.getName());
        return ResponseEntity.created(URI.create("/api/recursos/" + recurso.getId())).body(RecursoResponse.de(recurso));
    }
}
