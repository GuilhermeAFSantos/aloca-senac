package br.senac.alocasenac.reserva;

import java.time.Instant;

/** Período de uso de um recurso: semiaberto [início, término), decisão D-01. */
public record Intervalo(Instant inicio, Instant fim) {

    /** Cria um intervalo validando RN-T1 (término posterior ao início). */
    public static Intervalo de(Instant inicio, Instant fim) {
        if (inicio == null || fim == null) {
            throw new IntervaloInvalidoException("Informe o início e o término da reserva.");
        }
        if (!fim.isAfter(inicio)) {
            throw new IntervaloInvalidoException("O término precisa ser depois do início.");
        }
        return new Intervalo(inicio, fim);
    }

    /**
     * Indica se dois intervalos ocupam algum instante em comum (RN-T2).
     * Como o término não ocupa (D-01), intervalos que apenas se encostam não se sobrepõem.
     */
    public boolean sobrepoe(Intervalo outro) {
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }
}
