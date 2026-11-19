package br.senac.alocasenac.reserva;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Checagem prévia de sobreposição por recurso, inclusive agenda do professor (RF5, RN-T2; ADR-003, DP-04).
 * A garantia final contra dupla reserva é a restrição de exclusão do banco (I-4).
 *
 * <p>Considera reservas e bloqueios de manutenção (RF8, RN-T4): um recurso em manutenção não pode ser reservado.
 */
@Component
public class VerificadorDisponibilidade {

    /**
     * Retorna as ocupações existentes que impedem o pedido.
     *
     * @param reservaIgnorada reserva em alteração, que não conflita consigo mesma (pode ser nula)
     */
    public List<Ocupacao> conflitos(Set<Long> recursoIds, Intervalo pedido, Long reservaIgnorada,
            Collection<Ocupacao> existentes) {
        return existentes.stream()
                .filter(Ocupacao::isAtiva)
                .filter(ocupacao -> recursoIds.contains(ocupacao.getRecursoId()))
                .filter(ocupacao -> !ocupacao.pertenceAReserva(reservaIgnorada))
                .filter(ocupacao -> ocupacao.intervalo().sobrepoe(pedido))
                .toList();
    }
}
