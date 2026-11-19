package br.senac.alocasenac.emprestimo;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** RF9 — registro de retirada e devolução de materiais e equipamentos. */
class EmprestimoTest {

    private static final String JULIANA = "juliana.teste@example.invalid";

    private final Clock as9h = Clock.fixed(h("09:00"), ZoneOffset.UTC);
    private final Clock as11h = Clock.fixed(h("11:00"), ZoneOffset.UTC);

    @Test
    @DisplayName("RF9-C01: retirada registra instante e quem registrou")
    void RF9_C01_retiradaRegistraInstanteEResponsavel() {
        // Arrange
        Emprestimo emprestimo = new Emprestimo(10L, 20L);

        // Act
        emprestimo.retirar(JULIANA, as9h);

        // Assert
        assertThat(emprestimo.getRetiradaEm()).isEqualTo(h("09:00"));
        assertThat(emprestimo.getRetiradoPor()).isEqualTo(JULIANA);
        assertThat(emprestimo.getDevolvidoEm()).isNull();
    }

    @Test
    @DisplayName("RF9-C02: devolução de material retirado registra instante e quem registrou")
    void RF9_C02_devolucaoRegistraInstanteEResponsavel() {
        // Arrange
        Emprestimo emprestimo = new Emprestimo(10L, 20L);
        emprestimo.retirar(JULIANA, as9h);

        // Act
        emprestimo.devolver(JULIANA, as11h);

        // Assert
        assertThat(emprestimo.getDevolvidoEm()).isEqualTo(h("11:00"));
        assertThat(emprestimo.getDevolvidoPor()).isEqualTo(JULIANA);
    }

    @Test
    @DisplayName("RF9-C03: devolver material que não foi retirado é recusado")
    void RF9_C03_devolverSemRetiradaERecusado() {
        // Arrange
        Emprestimo emprestimo = new Emprestimo(10L, 20L);

        // Act + Assert
        assertThatThrownBy(() -> emprestimo.devolver(JULIANA, as11h))
                .isInstanceOf(EmprestimoInvalidoException.class);
        assertThat(emprestimo.getDevolvidoEm()).isNull();
    }

    @Test
    @DisplayName("RF9-C04: retirar duas vezes o mesmo item é recusado")
    void RF9_C04_retirarDuasVezesERecusado() {
        // Arrange
        Emprestimo emprestimo = new Emprestimo(10L, 20L);
        emprestimo.retirar(JULIANA, as9h);

        // Act + Assert
        assertThatThrownBy(() -> emprestimo.retirar(JULIANA, as11h))
                .isInstanceOf(EmprestimoInvalidoException.class);
        assertThat(emprestimo.getRetiradaEm()).isEqualTo(h("09:00"));
    }
}
