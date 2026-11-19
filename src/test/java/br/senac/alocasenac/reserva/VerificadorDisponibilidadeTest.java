package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * RN-T2 — Reservas do mesmo recurso não podem se sobrepor.
 * RF5 — Detecção de sobreposição, inclusive na agenda do professor.
 * Intervalos semiabertos [início, término), decisão D-01. Ocupação ativa de SOLICITADA conta, decisão D-02.
 */
class VerificadorDisponibilidadeTest {

    private static final long SALA = 1L;
    private static final long OUTRA_SALA = 2L;
    private static final long PROFESSOR = 7L;
    private static final long MATERIAL = 9L;
    private static final long RESERVA_EXISTENTE = 100L;
    private static final long RESERVA_EM_ALTERACAO = 200L;

    private final VerificadorDisponibilidade verificador = new VerificadorDisponibilidade();

    /** A sala já está ocupada das 09:00 às 10:00 por outra reserva. */
    private static Ocupacao salaOcupadaDas9As10() {
        return new Ocupacao(SALA, RESERVA_EXISTENTE, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true);
    }

    @Test
    @DisplayName("RN-T2-C01: mesmo recurso em outro horário é aceito")
    void RN_T2_C01_mesmoRecursoEmOutroHorarioEAceito() {
        // Arrange
        List<Ocupacao> existentes = List.of(salaOcupadaDas9As10());

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h("11:00"), h("12:00")), null,
                existentes);

        // Assert
        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("RN-T2-C02: outro recurso no mesmo horário é aceito")
    void RN_T2_C02_outroRecursoNoMesmoHorarioEAceito() {
        // Arrange
        List<Ocupacao> existentes = List.of(salaOcupadaDas9As10());

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(OUTRA_SALA), new Intervalo(h("09:00"), h("10:00")),
                null, existentes);

        // Assert
        assertThat(conflitos).isEmpty();
    }

    @ParameterizedTest(name = "{0}: pedido {1}–{2} sobre a sala ocupada 09:00–10:00 é recusado")
    @CsvSource({
        "RN-T2-C03 idêntico,         09:00,    10:00",
        "RN-T2-C04 contido,          09:30,    09:45",
        "RN-T2-C05 contém,           08:00,    11:00",
        "RN-T2-C06 cruza o início,   08:30,    09:30",
        "RN-T2-C07 cruza o fim,      09:30,    10:30",
        "RN-T2-C10 cruza 1 segundo,  09:59:59, 10:30"
    })
    void RN_T2_C03aC07eC10_sobreposicaoNoMesmoRecursoERecusada(String caso, String inicio, String fim) {
        // Arrange
        Ocupacao existente = salaOcupadaDas9As10();

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h(inicio), h(fim)), null,
                List.of(existente));

        // Assert
        assertThat(conflitos).as(caso).containsExactly(existente);
    }

    @ParameterizedTest(name = "{0}: pedido {1}–{2} encostado na ocupação 09:00–10:00 é aceito (D-01)")
    @CsvSource({
        "RN-T2-C08 começa no fim,      10:00, 11:00",
        "RN-T2-C09 termina no início,  08:00, 09:00"
    })
    void RN_T2_C08eC09_intervalosQueSeEncostamNaoSeSobrepoem(String caso, String inicio, String fim) {
        // Arrange
        List<Ocupacao> existentes = List.of(salaOcupadaDas9As10());

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h(inicio), h(fim)), null,
                existentes);

        // Assert
        assertThat(conflitos).as(caso).isEmpty();
    }

    @Test
    @DisplayName("RN-T2-C11: alterar reserva para horário ocupado por outra é recusado")
    void RN_T2_C11_alteracaoParaHorarioDeOutraReservaERecusada() {
        // Arrange
        Ocupacao outra = new Ocupacao(SALA, RESERVA_EXISTENTE, h("11:00"), h("12:00"), OrigemOcupacao.RESERVA, true);
        Ocupacao propria = new Ocupacao(SALA, RESERVA_EM_ALTERACAO, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA,
                true);

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h("11:30"), h("12:30")),
                RESERVA_EM_ALTERACAO, List.of(outra, propria));

        // Assert
        assertThat(conflitos).containsExactly(outra);
    }

    @Test
    @DisplayName("RN-T2-C12: alterar reserva dentro do próprio horário é aceito")
    void RN_T2_C12_alteracaoDentroDoProprioHorarioEAceita() {
        // Arrange
        Ocupacao propria = new Ocupacao(SALA, RESERVA_EM_ALTERACAO, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA,
                true);

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h("09:15"), h("10:00")),
                RESERVA_EM_ALTERACAO, List.of(propria));

        // Assert
        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("RN-T2-C14: ocupação ativa de reserva ainda SOLICITADA bloqueia o horário (D-02)")
    void RN_T2_C14_reservaPendenteJaOcupaOHorario() {
        // Arrange: ocupação ativa criada por uma reserva que aguarda aprovação
        Ocupacao pendente = new Ocupacao(SALA, RESERVA_EXISTENTE, h("14:00"), h("15:00"), OrigemOcupacao.RESERVA,
                true);

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h("14:30"), h("15:30")), null,
                List.of(pendente));

        // Assert
        assertThat(conflitos).containsExactly(pendente);
    }

    @Test
    @DisplayName("RN-T2-C15: ocupação inativa (reserva rejeitada ou cancelada) não bloqueia (D-02)")
    void RN_T2_C15_ocupacaoInativaNaoBloqueia() {
        // Arrange
        Ocupacao liberada = new Ocupacao(SALA, RESERVA_EXISTENTE, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA,
                false);

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA), new Intervalo(h("09:00"), h("10:00")), null,
                List.of(liberada));

        // Assert
        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("RF5-C01: professor já alocado em outra sala no horário é conflito")
    void RF5_C01_professorJaAlocadoEmOutraSalaEConflito() {
        // Arrange: professor dando aula na OUTRA_SALA das 09:00 às 10:00
        Ocupacao agendaDoProfessor = new Ocupacao(PROFESSOR, RESERVA_EXISTENTE, h("09:00"), h("10:00"),
                OrigemOcupacao.RESERVA, true);
        Ocupacao outraSala = new Ocupacao(OUTRA_SALA, RESERVA_EXISTENTE, h("09:00"), h("10:00"),
                OrigemOcupacao.RESERVA, true);

        // Act: pedido de SALA (livre) com o mesmo professor
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA, PROFESSOR), new Intervalo(h("09:30"),
                h("10:30")), null, List.of(agendaDoProfessor, outraSala));

        // Assert
        assertThat(conflitos).containsExactly(agendaDoProfessor);
    }

    @Test
    @DisplayName("RF5-C02: sala e professor livres são aceitos")
    void RF5_C02_salaEProfessorLivresSaoAceitos() {
        // Arrange
        List<Ocupacao> existentes = List.of(salaOcupadaDas9As10());

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(OUTRA_SALA, PROFESSOR), new Intervalo(h("09:00"),
                h("10:00")), null, existentes);

        // Assert
        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("RF5-C03: material já ocupado no horário é conflito, mesmo com sala e professor livres")
    void RF5_C03_materialOcupadoEConflito() {
        // Arrange
        Ocupacao projetor = new Ocupacao(MATERIAL, RESERVA_EXISTENTE, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA,
                true);

        // Act
        List<Ocupacao> conflitos = verificador.conflitos(Set.of(SALA, PROFESSOR, MATERIAL), new Intervalo(h("09:00"),
                h("10:00")), null, List.of(projetor));

        // Assert
        assertThat(conflitos).containsExactly(projetor);
    }
}
