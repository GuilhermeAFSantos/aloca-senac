package br.senac.alocasenac.reserva;

import java.util.List;

public class ConflitoDeHorarioException extends RuntimeException {

    private final transient List<Ocupacao> conflitos;

    public ConflitoDeHorarioException(List<Ocupacao> conflitos) {
        super("Recurso já ocupado no horário solicitado");
        this.conflitos = List.copyOf(conflitos);
    }

    public List<Ocupacao> getConflitos() {
        return conflitos;
    }
}
