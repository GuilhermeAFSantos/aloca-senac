package br.senac.alocasenac.relatorio;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.senac.alocasenac.reserva.Intervalo;
import br.senac.alocasenac.reserva.Ocupacao;
import br.senac.alocasenac.reserva.OrigemOcupacao;

/**
 * RF12-C07 — reserva fora do período do relatório não entra na utilização, e nunca a deixa negativa.
 * Lacuna de risco G2 da Aula 07 (RelatorioService.java:47-48).
 * Limite em trinca no início do período: termina 1 s antes (−1), termina exatamente no início (0, D-01).
 * O +1 (cruza o início em 1 s) depende da pergunta Q-13 e fica sem teste até a resposta. O mesmo vale no fim.
 */
class RelatorioPeriodoTest {

    private static final long SALA = 1L;
    private static final Intervalo PERIODO = new Intervalo(h("09:00"), h("17:00"));

    private final RelatorioService relatorio = new RelatorioService();

    @ParameterizedTest(name = "RF12-C07: {0}: reserva {1}–{2} não conta no período 09:00–17:00")
    @CsvSource({
        "termina 1 s antes do início (−1), 08:00, 08:59:59",
        "termina no início (0; D-01),      08:00, 09:00",
        "começa no fim (0; D-01),          17:00, 18:00",
        "começa 1 s depois do fim (+1),    17:00:01, 18:00"
    })
    void RF12_C07_reservaForaDoPeriodoNaoContaNaUtilizacao(String caso, String inicio, String fim) {
        // Arrange
        Ocupacao foraDoPeriodo = new Ocupacao(SALA, 1L, h(inicio), h(fim), OrigemOcupacao.RESERVA, true);

        // Act
        Relatorio r = relatorio.gerar(PERIODO, List.of(foraDoPeriodo), Set.of(SALA), List.of());

        // Assert
        assertThat(r.utilizacaoPorRecurso().getOrDefault(SALA, Duration.ZERO)).as(caso).isZero();
        assertThat(r.cargaHorariaPorProfessor().getOrDefault(SALA, Duration.ZERO)).as(caso).isZero();
    }
}
