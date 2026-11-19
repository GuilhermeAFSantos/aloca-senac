package br.senac.alocasenac.notificacao;

import static br.senac.alocasenac.Horarios.h;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;
import br.senac.alocasenac.reserva.ReservaService;

/** RF11 — notificação por outbox, com o Serviço de Notificação simulado por WireMock (RQ-2; ADR-005). */
class NotificacaoIntegracaoTest extends IntegracaoBase {

    private static final String ROTA = "/notificacoes";
    private static final WireMockServer SERVICO = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        SERVICO.start();
    }

    @DynamicPropertySource
    static void urlDoServico(DynamicPropertyRegistry registro) {
        registro.add("alocasenac.notificacao.url", SERVICO::baseUrl);
    }

    @AfterAll
    static void pararServico() {
        SERVICO.stop();
    }

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private DespachanteNotificacoes despachante;

    @BeforeEach
    void limparServico() {
        SERVICO.resetAll();
    }

    private Long sala() {
        return recursos.save(new Recurso("S-111", TipoRecurso.SALA, 30, "Bloco K", false)).getId();
    }

    @Test
    @DisplayName("RF11-C01: criar reserva registra uma notificação pendente na outbox")
    void RF11_C01_criarReservaRegistraNotificacaoPendente() {
        // Act
        reservaService.criar("mariana.teste@example.invalid", Set.of(sala()), h("09:00"), h("10:00"));

        // Assert
        List<NotificacaoOutbox> mensagens = outbox.findAll();
        assertThat(mensagens).hasSize(1);
        assertThat(mensagens.get(0).getEstado()).isEqualTo(EstadoNotificacao.PENDENTE);
        assertThat(mensagens.get(0).getDestinatario()).isEqualTo("mariana.teste@example.invalid");
    }

    @Test
    @DisplayName("RF11-C02: Serviço de Notificação fora do ar não desfaz a reserva e a notificação fica pendente")
    void RF11_C02_servicoForaDoArNaoDesfazAReserva() {
        // Arrange
        SERVICO.stubFor(post(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(503)));
        reservaService.criar("mariana.teste@example.invalid", Set.of(sala()), h("09:00"), h("10:00"));

        // Act
        despachante.despachar();

        // Assert
        assertThat(reservas.count()).as("reserva mantida").isEqualTo(1);
        List<NotificacaoOutbox> mensagens = outbox.findAll();
        assertThat(mensagens).hasSize(1);
        assertThat(mensagens.get(0).getEstado()).isEqualTo(EstadoNotificacao.PENDENTE);
        assertThat(mensagens.get(0).getTentativas()).isEqualTo(1);
    }

    @Test
    @DisplayName("RF11-C03: com o serviço disponível, a notificação pendente é enviada uma única vez")
    void RF11_C03_notificacaoPendenteEEnviadaUmaVez() {
        // Arrange
        SERVICO.stubFor(post(urlEqualTo(ROTA)).willReturn(aResponse().withStatus(202)));
        outbox.save(new NotificacaoOutbox("RESERVA_CRIADA", "mariana.teste@example.invalid", "{\"reservaId\":1}",
                EstadoNotificacao.PENDENTE));

        // Act
        despachante.despachar();
        despachante.despachar();

        // Assert
        assertThat(SERVICO.findAll(postRequestedFor(urlEqualTo(ROTA)))).hasSize(1);
        assertThat(outbox.findAll()).extracting(NotificacaoOutbox::getEstado)
                .containsExactly(EstadoNotificacao.ENVIADA);
    }
}
