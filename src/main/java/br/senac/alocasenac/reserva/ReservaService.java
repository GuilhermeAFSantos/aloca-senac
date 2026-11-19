package br.senac.alocasenac.reserva;

import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Service;

/** Criação, alteração e exclusão de reservas (RF4, RF5, RF6, RN-E4, RN-E5). */
@Service
public class ReservaService {

    public Reserva criar(String solicitante, Set<Long> recursoIds, Instant inicio, Instant fim) {
        return null;
    }

    public Reserva alterar(Long reservaId, Instant inicio, Instant fim, String ator) {
        return null;
    }

    public void apagar(Long reservaId, String ator) {
        // assinatura mínima: regra implementada na Aula 06
    }
}
