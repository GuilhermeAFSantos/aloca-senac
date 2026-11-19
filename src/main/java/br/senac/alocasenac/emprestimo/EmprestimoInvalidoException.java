package br.senac.alocasenac.emprestimo;

public class EmprestimoInvalidoException extends RuntimeException {

    public EmprestimoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
