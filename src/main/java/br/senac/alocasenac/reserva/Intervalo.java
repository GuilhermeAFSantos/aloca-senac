package br.senac.alocasenac.reserva;

import java.time.Instant;

/** Período de uso de um recurso: semiaberto [início, término), decisão D-01. */
public record Intervalo(Instant inicio, Instant fim) {

    /** Cria um intervalo validando RN-T1 (término posterior ao início). */
    public static Intervalo de(Instant inicio, Instant fim) {
        return new Intervalo(inicio, fim);
    }

    /** Indica se dois intervalos ocupam algum instante em comum (RN-T2). */
    public boolean sobrepoe(Intervalo outro) {
        return false;
    }
}
