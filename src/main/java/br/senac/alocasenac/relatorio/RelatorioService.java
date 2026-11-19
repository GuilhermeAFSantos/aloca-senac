package br.senac.alocasenac.relatorio;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.senac.alocasenac.reserva.Intervalo;
import br.senac.alocasenac.reserva.Ocupacao;
import br.senac.alocasenac.reserva.TentativaRecusada;

/** Relatório de utilização (RF12). */
@Service
public class RelatorioService {

    public Relatorio gerar(Intervalo periodo, List<Ocupacao> ocupacoes, Set<Long> idsDeProfessores,
            List<TentativaRecusada> recusas) {
        return new Relatorio(Map.of(), Map.of(), 0);
    }
}
