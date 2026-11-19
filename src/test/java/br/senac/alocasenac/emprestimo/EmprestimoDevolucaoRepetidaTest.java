package br.senac.alocasenac.emprestimo;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RF9-C08 — devolver de novo um item já devolvido é recusado, e o registro da primeira devolução é mantido.
 * Lacuna de risco G1 da Aula 07 (Emprestimo.java:51-52).
 */
class EmprestimoDevolucaoRepetidaTest {

    private static final String JULIANA = "juliana.teste@example.invalid";

    @Test
    @DisplayName("RF9-C08: segunda devolução é recusada e a primeira continua registrada")
    void RF9_C08_segundaDevolucaoERecusada() {
        // Arrange
        Emprestimo emprestimo = new Emprestimo(10L, 20L);
        emprestimo.retirar(JULIANA, Clock.fixed(h("09:00"), ZoneOffset.UTC));
        emprestimo.devolver(JULIANA, Clock.fixed(h("11:00"), ZoneOffset.UTC));

        // Act + Assert
        assertThatThrownBy(() -> emprestimo.devolver("carla.teste@example.invalid",
                Clock.fixed(h("12:00"), ZoneOffset.UTC)))
                .isInstanceOf(EmprestimoInvalidoException.class);
        assertThat(emprestimo.getDevolvidoEm()).isEqualTo(h("11:00"));
        assertThat(emprestimo.getDevolvidoPor()).isEqualTo(JULIANA);
    }
}
