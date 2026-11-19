package br.senac.alocasenac.recurso;

import java.util.List;

import org.springframework.stereotype.Service;

/** Pesquisa de recursos (RF3). */
@Service
public class CatalogoService {

    public List<Recurso> pesquisar(FiltroRecurso filtro) {
        return List.of();
    }
}
