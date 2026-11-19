package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/** B4 com banco real — auditoria das transições (RN-E5) e liberação do horário (D-02, D-11). */
class TransicoesReservaIntegracaoTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";
    private static final String JULIANA = "juliana.teste@example.invalid";

    @Autowired
    private ReservaService reservaService;

    private Reserva reservaDe(String solicitante, Long recursoId, EstadoReserva estado, String inicio, String fim) {
        Reserva reserva = reservas.save(new Reserva(solicitante, Set.of(recursoId), h(inicio), h(fim), estado));
        ocupacoes.save(new Ocupacao(recursoId, reserva.getId(), h(inicio), h(fim), OrigemOcupacao.RESERVA, true));
        return reserva;
    }

    private Long laboratorioL01() {
        return recursos.save(new Recurso("Laboratório L-01", TipoRecurso.SALA, 20, "Bloco B", true)).getId();
    }

    @Test
    @DisplayName("B4-04: toda transição gera auditoria")
    void B4_04_todaTransicaoGeraAuditoria() {
        // Arrange
        Reserva reserva = reservaDe(MARIANA, laboratorioL01(), EstadoReserva.SOLICITADA, "09:00", "10:00");

        // Act
        reservaService.aprovar(reserva.getId(), JULIANA);

        // Assert
        assertThat(auditoria.findAll())
                .filteredOn(r -> reserva.getId().equals(r.getEntidadeId()))
                .singleElement()
                .satisfies(r -> {
                    assertThat(r.getValorAnterior()).contains("SOLICITADA");
                    assertThat(r.getValorNovo()).contains("APROVADA");
                    assertThat(r.getAtor()).isEqualTo(JULIANA);
                });
    }
}
