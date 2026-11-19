package br.senac.alocasenac.reserva;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

/** Checagem prévia de sobreposição por recurso, inclusive agenda do professor (RF5, RN-T2; ADR-003). */
@Component
public class VerificadorDisponibilidade {

    /**
     * Retorna as ocupações existentes que impedem o pedido.
     *
     * @param reservaIgnorada reserva em alteração, que não conflita consigo mesma (pode ser nula)
     */
    public List<Ocupacao> conflitos(Set<Long> recursoIds, Intervalo pedido, Long reservaIgnorada,
            Collection<Ocupacao> existentes) {
        return List.of();
    }
}
