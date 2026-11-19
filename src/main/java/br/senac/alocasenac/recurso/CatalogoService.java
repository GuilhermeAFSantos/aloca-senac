package br.senac.alocasenac.recurso;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.senac.alocasenac.reserva.Ocupacao;
import br.senac.alocasenac.reserva.OcupacaoRepository;

/** Pesquisa de recursos por tipo, capacidade, localização e disponibilidade (RF3). */
@Service
public class CatalogoService {

    private final RecursoRepository recursos;
    private final OcupacaoRepository ocupacoes;

    public CatalogoService(RecursoRepository recursos, OcupacaoRepository ocupacoes) {
        this.recursos = recursos;
        this.ocupacoes = ocupacoes;
    }

    @Transactional(readOnly = true)
    public List<Recurso> pesquisar(FiltroRecurso filtro) {
        Set<Long> ocupados = filtro.disponivelEm() == null ? Set.of()
                : ocupacoes.findByAtivaTrue().stream()
                        .filter(ocupacao -> ocupacao.intervalo().sobrepoe(filtro.disponivelEm()))
                        .map(Ocupacao::getRecursoId)
                        .collect(Collectors.toSet());
        return recursos.findAll().stream()
                .filter(r -> filtro.tipo() == null || r.getTipo() == filtro.tipo())
                .filter(r -> filtro.capacidadeMinima() == null
                        || (r.getCapacidade() != null && r.getCapacidade() >= filtro.capacidadeMinima()))
                .filter(r -> filtro.localizacao() == null || filtro.localizacao().equalsIgnoreCase(r.getLocalizacao()))
                .filter(r -> !ocupados.contains(r.getId()))
                .toList();
    }
}
