package br.senac.alocasenac.notificacao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dispara o Despachante periodicamente; desligado nos testes, que despacham de forma controlada. */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "alocasenac.notificacao.agendamento", havingValue = "true")
public class AgendadorNotificacoes {

    private final DespachanteNotificacoes despachante;

    public AgendadorNotificacoes(DespachanteNotificacoes despachante) {
        this.despachante = despachante;
    }

    @Scheduled(fixedDelayString = "${alocasenac.notificacao.intervalo-ms}",
            initialDelayString = "${alocasenac.notificacao.intervalo-ms}")
    public void rodar() {
        despachante.despachar();
    }
}
