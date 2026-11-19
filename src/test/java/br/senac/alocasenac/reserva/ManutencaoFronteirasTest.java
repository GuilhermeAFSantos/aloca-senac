package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * B3 — recurso em manutenção não pode ser reservado (RF8, RN-T4).
 * Fronteiras do período pela decisão D-01: intervalo semiaberto [início, término).
 */
class ManutencaoFronteirasTest {

    private static final long SALA_S101 = 101L;

    private final VerificadorDisponibilidade verificador = new VerificadorDisponibilidade();

    @ParameterizedTest(name = "B3-04: {0}: reserva {1}–{2} diante da manutenção 08:00–10:00 é {3}")
    @CsvSource({
        "termina 1 segundo antes do início, 07:00,    07:59:59, aceita",
        "termina exatamente no início,      07:00,    08:00,    aceita",
        "cruza o início por 1 segundo,      07:00,    08:00:01, recusada",
        "fica dentro do período,            08:30,    09:30,    recusada",
        "cobre o período inteiro,           07:00,    11:00,    recusada",
        "cruza o fim por 1 segundo,         09:59:59, 11:00,    recusada",
        "começa exatamente no fim,          10:00,    11:00,    aceita",
        "começa 1 segundo depois do fim,    10:00:01, 11:00,    aceita"
    })
    void B3_04_reservaDianteDeUmPeriodoDeManutencao(String caso, String inicio, String termino, String resultado) {
        // Arrange: a Sala S-101 está em manutenção das 08:00 às 10:00
        Ocupacao manutencao = new Ocupacao(SALA_S101, null, h("08:00"), h("10:00"), OrigemOcupacao.BLOQUEIO, true);

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA_S101), new Intervalo(h(inicio), h(termino)), null,
                List.of(manutencao));

        // Assert
        if (resultado.equals("aceita")) {
            assertThat(conflitos).as(caso).isEmpty();
        } else {
            assertThat(conflitos).as(caso).containsExactly(manutencao);
        }
    }
}
