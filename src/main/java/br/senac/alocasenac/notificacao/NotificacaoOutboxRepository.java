package br.senac.alocasenac.notificacao;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoOutboxRepository extends JpaRepository<NotificacaoOutbox, Long> {
}
