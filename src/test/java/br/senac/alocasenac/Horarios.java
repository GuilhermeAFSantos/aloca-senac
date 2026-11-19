package br.senac.alocasenac;

import java.time.Instant;

/** Horários sintéticos de teste em um dia fixo (fuso de referência é a pergunta Q-02; usamos UTC). */
public final class Horarios {

    public static final String DIA = "2026-11-23";

    private Horarios() {
    }

    /** Ex.: {@code h("09:00")} ou {@code h("09:59:59")}. */
    public static Instant h(String hora) {
        String completa = hora.length() == 5 ? hora + ":00" : hora;
        return Instant.parse(DIA + "T" + completa + "Z");
    }
}
