package br.senac.alocasenac.notificacao;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Mensagem a notificar, gravada na transação da reserva (RF11; ADR-005). */
@Entity
public class NotificacaoOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String evento;

    private String destinatario;

    private String payload;

    @Enumerated(EnumType.STRING)
    private EstadoNotificacao estado;

    private int tentativas;

    protected NotificacaoOutbox() {
    }

    /** Construtor de reconstituição (persistência e fixtures de teste); não aplica regras. */
    public NotificacaoOutbox(String evento, String destinatario, String payload, EstadoNotificacao estado) {
        this.evento = evento;
        this.destinatario = destinatario;
        this.payload = payload;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getEvento() {
        return evento;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getPayload() {
        return payload;
    }

    public EstadoNotificacao getEstado() {
        return estado;
    }

    public int getTentativas() {
        return tentativas;
    }
}
