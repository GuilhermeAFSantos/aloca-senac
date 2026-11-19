package br.senac.alocasenac.recurso;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.senac.alocasenac.IntegracaoBase;

/**
 * RF3-C06 — na pesquisa por capacidade mínima, recurso sem capacidade (ex.: material) não entra no resultado.
 * Lacuna de risco G4 da Aula 07 (CatalogoService.java:35).
 */
class PesquisaCapacidadeTest extends IntegracaoBase {

    @Autowired
    private CatalogoService catalogo;

    @Test
    @DisplayName("RF3-C06: recurso sem capacidade é excluído da pesquisa por capacidade mínima")
    void RF3_C06_recursoSemCapacidadeEExcluidoDaPesquisaPorCapacidade() {
        // Arrange
        Recurso sala = recursos.save(new Recurso("S-980", TipoRecurso.SALA, 30, "Bloco N", false));
        recursos.save(new Recurso("Projetor P-09", TipoRecurso.MATERIAL, null, "Bloco N", false));

        // Act
        List<Recurso> resultado = catalogo.pesquisar(new FiltroRecurso(null, 30, null, null));

        // Assert
        assertThat(resultado).extracting(Recurso::getId).containsExactly(sala.getId());
    }
}
