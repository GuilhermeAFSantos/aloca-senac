package br.senac.alocasenac.notificacao;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente HTTP do Serviço de Notificação externo (RF11); simulado por WireMock nos testes (RQ-2). */
@Component
public class NotificacaoClient {

    private final RestClient http;

    public NotificacaoClient(@Value("${alocasenac.notificacao.url}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    /** Envia a mensagem; lança exceção se o serviço recusar ou estiver indisponível. */
    public void enviar(NotificacaoOutbox mensagem) {
        http.post()
                .uri("/notificacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("evento", mensagem.getEvento(), "destinatario", mensagem.getDestinatario(),
                        "payload", mensagem.getPayload()))
                .retrieve()
                .toBodilessEntity();
    }
}
