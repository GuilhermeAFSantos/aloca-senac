package br.senac.alocasenac.relatorio;

import java.time.Duration;
import java.util.Map;

/** Indicadores de RF12: utilização por recurso, carga horária por professor e conflitos evitados. */
public record Relatorio(Map<Long, Duration> utilizacaoPorRecurso, Map<Long, Duration> cargaHorariaPorProfessor,
        long conflitosEvitados) {
}
