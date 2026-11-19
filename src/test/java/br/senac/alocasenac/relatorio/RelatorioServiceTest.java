package br.senac.alocasenac.relatorio;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.senac.alocasenac.reserva.Intervalo;
import br.senac.alocasenac.reserva.MotivoRecusa;
import br.senac.alocasenac.reserva.Ocupacao;
import br.senac.alocasenac.reserva.OrigemOcupacao;
import br.senac.alocasenac.reserva.TentativaRecusada;

/** RF12 — utilização por recurso, carga horária alocada e conflitos evitados. */
class RelatorioServiceTest {

    private static final long SALA_101 = 1L;
    private static final long PROFESSOR = 7L;
    private static final Intervalo DIA_INTEIRO = new Intervalo(h("00:00"), h("23:59:59"));

    private final RelatorioService relatorio = new RelatorioService();

    private static Ocupacao reserva(long recursoId, long reservaId, String inicio, String fim) {
        return new Ocupacao(recursoId, reservaId, h(inicio), h(fim), OrigemOcupacao.RESERVA, true);
    }

    @Test
    @DisplayName("RF12-C01: duas reservas de 1 h na sala somam 2 h de utilização")
    void RF12_C01_utilizacaoSomaAsReservasDoRecurso() {
        // Arrange
        List<Ocupacao> ocupacoes = List.of(reserva(SALA_101, 1, "08:00", "09:00"), reserva(SALA_101, 2, "10:00",
                "11:00"));

        // Act
        Relatorio r = relatorio.gerar(DIA_INTEIRO, ocupacoes, Set.of(), List.of());

        // Assert
        assertThat(r.utilizacaoPorRecurso()).containsEntry(SALA_101, Duration.ofHours(2));
    }

    @Test
    @DisplayName("RF12-C02: professor alocado em três reservas de 1 h tem 3 h de carga horária")
    void RF12_C02_cargaHorariaDoProfessor() {
        // Arrange
        List<Ocupacao> ocupacoes = List.of(
                reserva(PROFESSOR, 1, "08:00", "09:00"),
                reserva(PROFESSOR, 2, "10:00", "11:00"),
                reserva(PROFESSOR, 3, "14:00", "15:00"),
                reserva(SALA_101, 1, "08:00", "09:00"));

        // Act
        Relatorio r = relatorio.gerar(DIA_INTEIRO, ocupacoes, Set.of(PROFESSOR), List.of());

        // Assert
        assertThat(r.cargaHorariaPorProfessor()).containsExactlyEntriesOf(java.util.Map.of(PROFESSOR,
                Duration.ofHours(3)));
    }

    @Test
    @DisplayName("RF12-C03: duas tentativas recusadas por sobreposição contam dois conflitos evitados")
    void RF12_C03_conflitosEvitadosContaAsRecusas() {
        // Arrange
        List<TentativaRecusada> recusas = List.of(
                new TentativaRecusada("mariana.teste@example.invalid", SALA_101, h("09:00"), h("10:00"),
                        MotivoRecusa.SOBREPOSICAO, h("08:00")),
                new TentativaRecusada("carla.teste@example.invalid", SALA_101, h("09:30"), h("10:30"),
                        MotivoRecusa.SOBREPOSICAO, h("08:05")));

        // Act
        Relatorio r = relatorio.gerar(DIA_INTEIRO, List.of(), Set.of(), recusas);

        // Assert
        assertThat(r.conflitosEvitados()).isEqualTo(2);
    }

    @Test
    @DisplayName("RF12-C04: período sem reservas tem utilização, carga e conflitos zerados")
    void RF12_C04_periodoSemReservasZerado() {
        // Act
        Relatorio r = relatorio.gerar(DIA_INTEIRO, List.of(), Set.of(PROFESSOR), List.of());

        // Assert
        assertThat(r.utilizacaoPorRecurso().getOrDefault(SALA_101, Duration.ZERO)).isZero();
        assertThat(r.cargaHorariaPorProfessor().getOrDefault(PROFESSOR, Duration.ZERO)).isZero();
        assertThat(r.conflitosEvitados()).isZero();
    }
}
