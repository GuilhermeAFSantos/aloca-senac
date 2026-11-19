package br.senac.alocasenac.auditoria;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** RN-E5-C05 — o instante da auditoria vem do Clock injetado (determinístico). */
class RegistroAuditoriaTest {

    @Test
    @DisplayName("RN-E5-C05: o registro de auditoria usa o instante do relógio injetado e guarda os dados da mudança")
    void RN_E5_C05_registroUsaOInstanteDoRelogioInjetado() {
        // Arrange
        Clock relogio = Clock.fixed(h("08:15"), ZoneOffset.UTC);

        // Act
        RegistroAuditoria registro = RegistroAuditoria.de("Reserva", 42L, "CRIACAO", null, "SOLICITADA",
                "mariana.teste@example.invalid", relogio);

        // Assert
        assertThat(registro.getInstante()).isEqualTo(h("08:15"));
        assertThat(registro.getEntidade()).isEqualTo("Reserva");
        assertThat(registro.getEntidadeId()).isEqualTo(42L);
        assertThat(registro.getAcao()).isEqualTo("CRIACAO");
        assertThat(registro.getValorNovo()).isEqualTo("SOLICITADA");
        assertThat(registro.getAtor()).isEqualTo("mariana.teste@example.invalid");
    }
}
