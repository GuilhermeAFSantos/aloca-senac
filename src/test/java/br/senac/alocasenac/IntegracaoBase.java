package br.senac.alocasenac;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import br.senac.alocasenac.auditoria.RegistroAuditoriaRepository;
import br.senac.alocasenac.notificacao.NotificacaoOutboxRepository;
import br.senac.alocasenac.recurso.RecursoRepository;
import br.senac.alocasenac.reserva.OcupacaoRepository;
import br.senac.alocasenac.reserva.ReservaRepository;
import br.senac.alocasenac.reserva.TentativaRecusadaRepository;

/**
 * Base dos testes de integração: PostgreSQL real em container (RQ-2, RQ-7) e MockMvc.
 * O esquema vem das migrações do Flyway (Aula 06), o mesmo da aplicação.
 * O agendador da outbox fica desligado para os testes controlarem quando a notificação é despachada.
 */
@SpringBootTest(properties = "alocasenac.notificacao.agendamento=false")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegracaoBase {

    @Autowired
    protected RecursoRepository recursos;

    @Autowired
    protected ReservaRepository reservas;

    @Autowired
    protected OcupacaoRepository ocupacoes;

    @Autowired
    protected TentativaRecusadaRepository tentativasRecusadas;

    @Autowired
    protected RegistroAuditoriaRepository auditoria;

    @Autowired
    protected NotificacaoOutboxRepository outbox;

    @BeforeEach
    void limparBanco() {
        outbox.deleteAll();
        auditoria.deleteAll();
        tentativasRecusadas.deleteAll();
        ocupacoes.deleteAll();
        reservas.deleteAll();
        recursos.deleteAll();
    }
}
