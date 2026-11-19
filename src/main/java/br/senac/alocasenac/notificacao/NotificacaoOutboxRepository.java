package br.senac.alocasenac.notificacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoOutboxRepository extends JpaRepository<NotificacaoOutbox, Long> {

    List<NotificacaoOutbox> findByEstadoOrderByIdAsc(EstadoNotificacao estado);
}
