package br.senac.alocasenac.auditoria;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.senac.alocasenac.IntegracaoBase;

/** RF10-C01 — histórico auditável de uma entidade em ordem cronológica. */
class HistoricoAuditoriaTest extends IntegracaoBase {

    @Autowired
    private AuditoriaService auditoriaService;

    @Test
    @DisplayName("RF10-C01: histórico de uma reserva vem em ordem cronológica, com ator e mudança, só daquela reserva")
    void RF10_C01_historicoEmOrdemCronologicaSoDaEntidade() {
        // Arrange: gravados fora de ordem, mais um registro de outra reserva
        auditoria.save(new RegistroAuditoria("Reserva", 42L, "ALTERACAO", "09:00-10:00", "11:00-12:00",
                "mariana.teste@example.invalid", h("10:00")));
        auditoria.save(new RegistroAuditoria("Reserva", 42L, "CRIACAO", null, "SOLICITADA",
                "mariana.teste@example.invalid", h("08:00")));
        auditoria.save(new RegistroAuditoria("Reserva", 43L, "CRIACAO", null, "SOLICITADA",
                "carla.teste@example.invalid", h("09:00")));

        // Act
        List<RegistroAuditoria> historico = auditoriaService.historico("Reserva", 42L);

        // Assert
        assertThat(historico).extracting(RegistroAuditoria::getAcao).containsExactly("CRIACAO", "ALTERACAO");
        assertThat(historico).extracting(RegistroAuditoria::getAtor)
                .containsOnly("mariana.teste@example.invalid");
    }
}
