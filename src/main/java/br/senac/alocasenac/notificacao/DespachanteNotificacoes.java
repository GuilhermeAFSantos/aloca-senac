package br.senac.alocasenac.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Lê a outbox e envia ao Serviço de Notificação (RF11; ADR-005).
 * Falha no envio não afeta a reserva: a mensagem continua pendente e é reenviada na próxima rodada.
 */
@Component
public class DespachanteNotificacoes {

    private static final Logger LOG = LoggerFactory.getLogger(DespachanteNotificacoes.class);

    private final NotificacaoOutboxRepository outbox;
    private final NotificacaoClient cliente;

    public DespachanteNotificacoes(NotificacaoOutboxRepository outbox, NotificacaoClient cliente) {
        this.outbox = outbox;
        this.cliente = cliente;
    }

    public void despachar() {
        for (NotificacaoOutbox mensagem : outbox.findByEstadoOrderByIdAsc(EstadoNotificacao.PENDENTE)) {
            try {
                cliente.enviar(mensagem);
                mensagem.marcarEnviada();
            } catch (RestClientException falha) {
                mensagem.registrarFalha();
                LOG.warn("Notificação {} não enviada (tentativa {}): {}", mensagem.getId(), mensagem.getTentativas(),
                        falha.getMessage());
            }
            outbox.save(mensagem);
        }
    }
}
