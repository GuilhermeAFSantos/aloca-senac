package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.auditoria.RegistroAuditoria;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/** RF5, RF6, RN-T2, RN-T3 e RN-E5 com PostgreSQL real (ADR-003, ADR-004). */
class ReservaServiceIntegracaoTest extends IntegracaoBase {

    private static final String MARIANA = "mariana.teste@example.invalid";
    private static final String CARLA = "carla.teste@example.invalid";

    @Autowired
    private ReservaService reservaService;

    private Recurso sala(String nome) {
        return recursos.save(new Recurso(nome, TipoRecurso.SALA, 30, "Bloco A", false));
    }

    /** Dispara as chamadas ao mesmo tempo e conta quantas foram aceitas (retornaram sem exceção). */
    private int dispararSimultaneamente(List<Callable<Reserva>> chamadas) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(chamadas.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Reserva>> futuros = new ArrayList<>();
            for (Callable<Reserva> chamada : chamadas) {
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return chamada.call();
                }));
            }
            largada.countDown();
            int aceitas = 0;
            for (Future<Reserva> futuro : futuros) {
                try {
                    futuro.get(30, TimeUnit.SECONDS);
                    aceitas++;
                } catch (java.util.concurrent.ExecutionException recusada) {
                    // recusa esperada para as perdedoras da disputa
                }
            }
            return aceitas;
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Reserva> pedido(String solicitante, Long recursoId, Instant inicio, Instant fim) {
        return () -> reservaService.criar(solicitante, Set.of(recursoId), inicio, fim);
    }

    @Test
    @DisplayName("RN-T3-C01: duas solicitações simultâneas do mesmo recurso e intervalo geram uma única reserva")
    void RN_T3_C01_duasSimultaneasGeramUmaUnicaReserva() throws Exception {
        // Arrange
        Long salaId = sala("S-101").getId();

        // Act
        int aceitas = dispararSimultaneamente(List.of(
                pedido(MARIANA, salaId, h("09:00"), h("10:00")),
                pedido(CARLA, salaId, h("09:00"), h("10:00"))));

        // Assert
        assertThat(aceitas).as("reservas aceitas").isEqualTo(1);
        assertThat(reservas.count()).as("reservas gravadas").isEqualTo(1);
    }

    @Test
    @DisplayName("RN-T3-C02: vinte solicitações simultâneas do mesmo recurso geram uma única reserva")
    void RN_T3_C02_vinteSimultaneasGeramUmaUnicaReserva() throws Exception {
        // Arrange
        Long salaId = sala("S-102").getId();
        List<Callable<Reserva>> chamadas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            chamadas.add(pedido("solicitante" + i + "@example.invalid", salaId, h("09:00"), h("10:00")));
        }

        // Act
        int aceitas = dispararSimultaneamente(chamadas);

        // Assert
        assertThat(aceitas).as("reservas aceitas").isEqualTo(1);
        assertThat(reservas.count()).as("reservas gravadas").isEqualTo(1);
    }

    @Test
    @DisplayName("RN-T3-C03: duas simultâneas com intervalos parcialmente sobrepostos geram uma única reserva")
    void RN_T3_C03_simultaneasParcialmenteSobrepostasGeramUmaReserva() throws Exception {
        // Arrange
        Long salaId = sala("S-103").getId();

        // Act
        int aceitas = dispararSimultaneamente(List.of(
                pedido(MARIANA, salaId, h("09:00"), h("10:00")),
                pedido(CARLA, salaId, h("09:30"), h("10:30"))));

        // Assert
        assertThat(aceitas).as("reservas aceitas").isEqualTo(1);
        assertThat(reservas.count()).as("reservas gravadas").isEqualTo(1);
    }

    @Test
    @DisplayName("RN-T3-C04: duas simultâneas de recursos diferentes são ambas aceitas")
    void RN_T3_C04_simultaneasDeRecursosDiferentesSaoAceitas() throws Exception {
        // Arrange
        Long salaA = sala("S-104").getId();
        Long salaB = sala("S-105").getId();

        // Act
        int aceitas = dispararSimultaneamente(List.of(
                pedido(MARIANA, salaA, h("09:00"), h("10:00")),
                pedido(CARLA, salaB, h("09:00"), h("10:00"))));

        // Assert
        assertThat(aceitas).as("reservas aceitas").isEqualTo(2);
        assertThat(reservas.count()).as("reservas gravadas").isEqualTo(2);
    }

    @Test
    @DisplayName("RN-T3-C05: a solicitação perdedora da disputa é registrada como tentativa recusada")
    void RN_T3_C05_perdedoraDaDisputaERegistradaComoTentativaRecusada() throws Exception {
        // Arrange
        Long salaId = sala("S-106").getId();

        // Act
        dispararSimultaneamente(List.of(
                pedido(MARIANA, salaId, h("09:00"), h("10:00")),
                pedido(CARLA, salaId, h("09:00"), h("10:00"))));

        // Assert: o motivo depende de onde a disputa é detectada (checagem prévia ou banco)
        List<TentativaRecusada> recusas = tentativasRecusadas.findAll();
        assertThat(recusas).hasSize(1);
        assertThat(recusas.get(0).getMotivo()).isIn(MotivoRecusa.SOBREPOSICAO, MotivoRecusa.CONCORRENCIA);
        assertThat(recusas.get(0).getRecursoId()).isEqualTo(salaId);
    }

    @Test
    @DisplayName("RF5-C04: reserva com vários recursos e um em conflito é recusada por inteiro")
    void RF5_C04_reservaComUmRecursoEmConflitoERecusadaPorInteiro() {
        // Arrange
        Long salaLivre = sala("S-107").getId();
        Long projetor = recursos.save(new Recurso("Projetor P-01", TipoRecurso.MATERIAL, null, "Bloco A", false))
                .getId();
        ocupacoes.save(new Ocupacao(projetor, 999L, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true));

        // Act + Assert
        assertThatThrownBy(() -> reservaService.criar(MARIANA, Set.of(salaLivre, projetor), h("09:00"), h("10:00")))
                .isInstanceOf(ConflitoDeHorarioException.class);
        assertThat(reservas.count()).as("nenhuma reserva gravada").isZero();
        assertThat(ocupacoes.count()).as("só a ocupação pré-existente").isEqualTo(1);
    }

    @Test
    @DisplayName("RN-T2-C13: sobreposição gravada direto no banco é rejeitada pela restrição de exclusão")
    void RN_T2_C13_sobreposicaoGravadaDiretoNoBancoERejeitada() {
        // Arrange
        Long salaId = sala("S-108").getId();
        ocupacoes.saveAndFlush(new Ocupacao(salaId, 1L, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true));

        // Act + Assert
        assertThatThrownBy(() -> ocupacoes
                .saveAndFlush(new Ocupacao(salaId, 2L, h("09:30"), h("10:30"), OrigemOcupacao.RESERVA, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("RN-E5-C01: criar reserva gera um registro de auditoria com estado, ator e instante")
    void RN_E5_C01_criarReservaGeraAuditoria() {
        // Arrange: recurso restrito, que fica SOLICITADA (D-05: não restrito é aprovado pelo sistema, ver B1-09)
        Long salaId = recursos.save(new Recurso("L-109", TipoRecurso.SALA, 30, "Bloco A", true)).getId();

        // Act
        Reserva criada = reservaService.criar(MARIANA, Set.of(salaId), h("09:00"), h("10:00"));

        // Assert
        assertThat(criada).isNotNull();
        List<RegistroAuditoria> registros = auditoria.findAll();
        assertThat(registros).hasSize(1);
        RegistroAuditoria registro = registros.get(0);
        assertThat(registro.getEntidade()).isEqualTo("Reserva");
        assertThat(registro.getEntidadeId()).isEqualTo(criada.getId());
        assertThat(registro.getValorNovo()).contains("SOLICITADA");
        assertThat(registro.getAtor()).isEqualTo(MARIANA);
        assertThat(registro.getInstante()).isNotNull();
    }

    @Test
    @DisplayName("RN-E5-C02: alterar reserva gera auditoria com valor anterior e novo")
    void RN_E5_C02_alterarReservaGeraAuditoriaComAntesEDepois() {
        // Arrange
        Long salaId = sala("S-110").getId();
        Reserva existente = reservas.save(new Reserva(MARIANA, Set.of(salaId), h("09:00"), h("10:00"),
                EstadoReserva.SOLICITADA));
        ocupacoes.save(new Ocupacao(salaId, existente.getId(), h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true));

        // Act
        reservaService.alterar(existente.getId(), h("11:00"), h("12:00"), MARIANA);

        // Assert
        List<RegistroAuditoria> registros = auditoria.findAll();
        assertThat(registros).hasSize(1);
        assertThat(registros.get(0).getAcao()).isEqualTo("ALTERACAO");
        assertThat(registros.get(0).getValorAnterior()).contains(h("09:00").toString());
        assertThat(registros.get(0).getValorNovo()).contains(h("11:00").toString());
    }
}
