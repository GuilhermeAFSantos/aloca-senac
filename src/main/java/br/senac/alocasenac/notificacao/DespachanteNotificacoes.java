package br.senac.alocasenac.notificacao;

import org.springframework.stereotype.Component;

/** Lê a outbox e envia ao Serviço de Notificação com retentativa (RF11; ADR-005). */
@Component
public class DespachanteNotificacoes {

    public void despachar() {
        // assinatura mínima: regra implementada na Aula 06
    }
}
