package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/**
 * RN-T3-C06 — duas alterações simultâneas para o mesmo horário livre produzem uma única reserva nesse horário.
 * Lacuna de risco G6 da Aula 07 (ReservaService.java:68-69): dupla reserva por alteração (LIM-4).
 */
class ReservaAlteracaoConcorrenteTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";
    private static final String CARLA = "carla.teste@example.invalid";

    @Autowired
    private ReservaService reservaService;

    private Reserva reservaExistente(Long salaId, String solicitante, String inicio, String fim) {
        Reserva reserva = reservas.save(new Reserva(solicitante, Set.of(salaId), h(inicio), h(fim),
                EstadoReserva.SOLICITADA));
        ocupacoes.save(new Ocupacao(salaId, reserva.getId(), h(inicio), h(fim), OrigemOcupacao.RESERVA, true));
        return reserva;
    }

    @Test
    @DisplayName("RN-T3-C06: duas alterações simultâneas para o mesmo horário: só uma é aceita, a outra recebe conflito")
    void RN_T3_C06_alteracoesSimultaneasParaOMesmoHorarioAceitamUmaSo() throws Exception {
        // Arrange: duas reservas da mesma sala em horários diferentes
        Long salaId = recursos.save(new Recurso("S-960", TipoRecurso.SALA, 30, "Bloco L", false)).getId();
        Reserva daMariana = reservaExistente(salaId, MARIANA, "09:00", "10:00");
        Reserva daCarla = reservaExistente(salaId, CARLA, "11:00", "12:00");

        // Act: as duas tentam ir para 14:00–15:00 ao mesmo tempo
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Reserva>> futuros = new ArrayList<>();
        try {
            for (Reserva reserva : List.of(daMariana, daCarla)) {
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return reservaService.alterar(reserva.getId(), h("14:00"), h("15:00"), reserva.getSolicitante());
                }));
            }
            largada.countDown();
            int aceitas = 0;
            List<Throwable> recusas = new ArrayList<>();
            for (Future<Reserva> futuro : futuros) {
                try {
                    futuro.get(30, TimeUnit.SECONDS);
                    aceitas++;
                } catch (ExecutionException recusada) {
                    recusas.add(recusada.getCause());
                }
            }

            // Assert
            assertThat(aceitas).as("alterações aceitas").isEqualTo(1);
            assertThat(recusas).singleElement().isInstanceOf(ConflitoDeHorarioException.class);
            long noHorarioDisputado = ocupacoes.findAll().stream()
                    .filter(o -> o.intervalo().inicio().equals(h("14:00")))
                    .count();
            assertThat(noHorarioDisputado).as("ocupações em 14:00–15:00").isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
