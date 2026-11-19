package br.senac.alocasenac.reserva;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Registro de Tentativas Recusadas: grava em transação própria, porque a transação recusada é desfeita (DP-06). */
@Component
public class RegistroDeRecusas {

    private final TentativaRecusadaRepository tentativas;
    private final Clock clock;

    public RegistroDeRecusas(TentativaRecusadaRepository tentativas, Clock clock) {
        this.tentativas = tentativas;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String solicitante, Collection<Long> recursoIds, Instant inicio, Instant fim,
            MotivoRecusa motivo) {
        Instant agora = clock.instant();
        for (Long recursoId : recursoIds) {
            tentativas.save(new TentativaRecusada(solicitante, recursoId, inicio, fim, motivo, agora));
        }
    }
}
