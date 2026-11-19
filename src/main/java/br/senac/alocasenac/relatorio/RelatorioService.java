package br.senac.alocasenac.relatorio;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.senac.alocasenac.reserva.Intervalo;
import br.senac.alocasenac.reserva.Ocupacao;
import br.senac.alocasenac.reserva.OrigemOcupacao;
import br.senac.alocasenac.reserva.TentativaRecusada;

/**
 * Relatório de utilização (RF12).
 *
 * <p>Comportamento provisório, sem regra na fonte: conta apenas a parte da reserva dentro do período (Q-13)
 * e ignora ocupações inativas de reservas rejeitadas ou canceladas (Q-14).
 */
@Service
public class RelatorioService {

    public Relatorio gerar(Intervalo periodo, List<Ocupacao> ocupacoes, Set<Long> idsDeProfessores,
            List<TentativaRecusada> recusas) {
        Map<Long, Duration> utilizacao = new HashMap<>();
        Map<Long, Duration> cargaHoraria = new HashMap<>();
        for (Ocupacao ocupacao : ocupacoes) {
            if (!ocupacao.isAtiva() || ocupacao.getOrigem() != OrigemOcupacao.RESERVA) {
                continue;
            }
            Duration dentroDoPeriodo = duracaoDentro(ocupacao.intervalo(), periodo);
            if (dentroDoPeriodo.isZero()) {
                continue;
            }
            utilizacao.merge(ocupacao.getRecursoId(), dentroDoPeriodo, Duration::plus);
            if (idsDeProfessores.contains(ocupacao.getRecursoId())) {
                cargaHoraria.merge(ocupacao.getRecursoId(), dentroDoPeriodo, Duration::plus);
            }
        }
        return new Relatorio(Map.copyOf(utilizacao), Map.copyOf(cargaHoraria), recusas.size());
    }

    private static Duration duracaoDentro(Intervalo intervalo, Intervalo periodo) {
        if (!intervalo.sobrepoe(periodo)) {
            return Duration.ZERO;
        }
        Instant inicio = intervalo.inicio().isAfter(periodo.inicio()) ? intervalo.inicio() : periodo.inicio();
        Instant fim = intervalo.fim().isBefore(periodo.fim()) ? intervalo.fim() : periodo.fim();
        return Duration.between(inicio, fim);
    }
}
