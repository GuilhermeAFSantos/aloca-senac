package br.senac.alocasenac.reserva;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.auditoria.AuditoriaService;
import br.senac.alocasenac.recurso.Recurso;
import br.senac.alocasenac.recurso.TipoRecurso;

/**
 * RN-E5-C04 — se a auditoria falhar, a mudança também é desfeita (ADR-004).
 * Injeção de falha na auditoria; a persistência continua real (RQ-7).
 */
class ReservaAuditoriaFalhaTest extends IntegracaoBase {

    @MockitoSpyBean
    private AuditoriaService auditoriaService;

    @Autowired
    private ReservaService reservaService;

    @Test
    @DisplayName("RN-E5-C04: falha ao gravar a auditoria desfaz a criação da reserva")
    void RN_E5_C04_falhaNaAuditoriaDesfazAReserva() {
        // Arrange
        Long salaId = recursos.save(new Recurso("S-201", TipoRecurso.SALA, 30, "Bloco B", false)).getId();
        doThrow(new IllegalStateException("falha simulada na auditoria"))
                .when(auditoriaService).registrar(any(), anyLong(), any(), any(), any(), any());

        // Act + Assert
        assertThatThrownBy(() -> reservaService.criar("mariana.teste@example.invalid", Set.of(salaId), h("09:00"),
                h("10:00")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(reservas.count()).as("reserva desfeita").isZero();
        assertThat(ocupacoes.count()).as("ocupação desfeita").isZero();
    }
}
