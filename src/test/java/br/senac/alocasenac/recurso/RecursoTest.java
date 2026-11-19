package br.senac.alocasenac.recurso;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** RF2 — cadastro exige os dados obrigatórios. */
class RecursoTest {

    @ParameterizedTest(name = "RF2-C02: cadastro com nome=''{0}'' e tipo={1} é recusado")
    @CsvSource(nullValues = "null", value = { "null, SALA", "'', SALA", "'   ', PROFESSOR", "Projetor P-01, null" })
    void RF2_C02_cadastroSemNomeOuSemTipoERecusado(String nome, TipoRecurso tipo) {
        // Act + Assert
        assertThatThrownBy(() -> Recurso.novo(nome, tipo, 30, "Bloco A", false))
                .isInstanceOf(DadosInvalidosException.class);
    }
}
