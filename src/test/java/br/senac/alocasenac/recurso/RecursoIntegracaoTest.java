package br.senac.alocasenac.recurso;

import static br.senac.alocasenac.Horarios.h;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import br.senac.alocasenac.IntegracaoBase;
import br.senac.alocasenac.auditoria.RegistroAuditoria;
import br.senac.alocasenac.reserva.Intervalo;
import br.senac.alocasenac.reserva.Ocupacao;
import br.senac.alocasenac.reserva.OrigemOcupacao;

/** RF2 (cadastro e consulta), RF3 (pesquisa) e RN-E5-C03 (auditoria de cadastro). */
class RecursoIntegracaoTest extends IntegracaoBase {

    private static final String ROBERTO = "roberto.teste@example.invalid";

    @Autowired
    private RecursoService recursoService;

    @Autowired
    private CatalogoService catalogo;

    @ParameterizedTest(name = "RF2-C01: recurso do tipo {0} cadastrado pode ser consultado")
    @EnumSource(TipoRecurso.class)
    void RF2_C01_recursoCadastradoPodeSerConsultado(TipoRecurso tipo) {
        // Arrange
        NovoRecursoRequest dados = new NovoRecursoRequest("Recurso " + tipo, tipo, 20, "Bloco A", false);

        // Act
        Recurso cadastrado = recursoService.cadastrar(dados, ROBERTO);

        // Assert
        assertThat(cadastrado).isNotNull();
        Recurso consultado = recursos.findById(cadastrado.getId()).orElseThrow();
        assertThat(consultado.getNome()).isEqualTo("Recurso " + tipo);
        assertThat(consultado.getTipo()).isEqualTo(tipo);
    }

    @Test
    @DisplayName("RN-E5-C03: cadastro de recurso gera auditoria com o Administrador como ator")
    void RN_E5_C03_cadastroDeRecursoGeraAuditoria() {
        // Arrange
        NovoRecursoRequest dados = new NovoRecursoRequest("S-401", TipoRecurso.SALA, 40, "Bloco D", true);

        // Act
        Recurso cadastrado = recursoService.cadastrar(dados, ROBERTO);

        // Assert
        assertThat(cadastrado).isNotNull();
        List<RegistroAuditoria> registros = auditoria.findAll();
        assertThat(registros).hasSize(1);
        assertThat(registros.get(0).getEntidade()).isEqualTo("Recurso");
        assertThat(registros.get(0).getEntidadeId()).isEqualTo(cadastrado.getId());
        assertThat(registros.get(0).getAcao()).isEqualTo("CRIACAO");
        assertThat(registros.get(0).getAtor()).isEqualTo(ROBERTO);
    }

    @Test
    @DisplayName("RF3-C01: pesquisa por tipo SALA retorna só salas")
    void RF3_C01_pesquisaPorTipoRetornaSoOTipo() {
        // Arrange
        Recurso sala = recursos.save(new Recurso("S-501", TipoRecurso.SALA, 30, "Bloco E", false));
        recursos.save(new Recurso("Projetor P-02", TipoRecurso.MATERIAL, null, "Bloco E", false));

        // Act
        List<Recurso> resultado = catalogo.pesquisar(new FiltroRecurso(TipoRecurso.SALA, null, null, null));

        // Assert
        assertThat(resultado).extracting(Recurso::getId).containsExactly(sala.getId());
    }

    @Test
    @DisplayName("RF3-C02: pesquisa por capacidade mínima 30 retorna as de 30 e 31, não a de 29")
    void RF3_C02_pesquisaPorCapacidadeMinimaRespeitaOLimite() {
        // Arrange
        recursos.save(new Recurso("S-29", TipoRecurso.SALA, 29, "Bloco F", false));
        Recurso de30 = recursos.save(new Recurso("S-30", TipoRecurso.SALA, 30, "Bloco F", false));
        Recurso de31 = recursos.save(new Recurso("S-31", TipoRecurso.SALA, 31, "Bloco F", false));

        // Act
        List<Recurso> resultado = catalogo.pesquisar(new FiltroRecurso(null, 30, null, null));

        // Assert
        assertThat(resultado).extracting(Recurso::getId).containsExactlyInAnyOrder(de30.getId(), de31.getId());
    }

    @Test
    @DisplayName("RF3-C03: pesquisa por localização retorna só recursos daquela localização")
    void RF3_C03_pesquisaPorLocalizacao() {
        // Arrange
        Recurso blocoG = recursos.save(new Recurso("S-601", TipoRecurso.SALA, 30, "Bloco G", false));
        recursos.save(new Recurso("S-701", TipoRecurso.SALA, 30, "Bloco H", false));

        // Act
        List<Recurso> resultado = catalogo.pesquisar(new FiltroRecurso(null, null, "Bloco G", null));

        // Assert
        assertThat(resultado).extracting(Recurso::getId).containsExactly(blocoG.getId());
    }

    @Test
    @DisplayName("RF3-C04: pesquisa por disponibilidade exclui recursos ocupados no intervalo")
    void RF3_C04_pesquisaPorDisponibilidadeExcluiOcupados() {
        // Arrange
        Recurso livre = recursos.save(new Recurso("S-801", TipoRecurso.SALA, 30, "Bloco I", false));
        Recurso ocupada = recursos.save(new Recurso("S-802", TipoRecurso.SALA, 30, "Bloco I", false));
        ocupacoes.save(new Ocupacao(ocupada.getId(), 1L, h("09:00"), h("10:00"), OrigemOcupacao.RESERVA, true));

        // Act
        List<Recurso> resultado = catalogo.pesquisar(new FiltroRecurso(TipoRecurso.SALA, null, null,
                new Intervalo(h("09:00"), h("10:00"))));

        // Assert
        assertThat(resultado).extracting(Recurso::getId).containsExactly(livre.getId());
    }
}
