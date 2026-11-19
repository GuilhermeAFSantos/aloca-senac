package br.senac.alocasenac.reserva;

public class OperacaoProibidaException extends RuntimeException {

    public OperacaoProibidaException(String mensagem) {
        super(mensagem);
    }
}
