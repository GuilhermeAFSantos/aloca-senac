package br.senac.alocasenac.reserva;

/** Mudança de estado fora das transições permitidas (RN-E1, RN-E2). */
public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
