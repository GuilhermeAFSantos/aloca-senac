package br.senac.alocasenac.config;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.senac.alocasenac.emprestimo.EmprestimoInvalidoException;
import br.senac.alocasenac.recurso.DadosInvalidosException;
import br.senac.alocasenac.reserva.ConflitoDeHorarioException;
import br.senac.alocasenac.reserva.IntervaloInvalidoException;
import br.senac.alocasenac.reserva.OperacaoProibidaException;

/**
 * Traduz as regras violadas em respostas compreensíveis, sem detalhes internos (RF13, RQ-6).
 * Convenção da API: 400 entrada inválida · 404 não encontrado · 409 operação proibida ou conflito de horário.
 */
@RestControllerAdvice
public class TratamentoDeErros {

    @ExceptionHandler({ IntervaloInvalidoException.class, DadosInvalidosException.class })
    ProblemDetail entradaInvalida(RuntimeException erro) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", erro.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoIlegivel(HttpMessageNotReadableException erro) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "O corpo da requisição não pôde ser lido.");
    }

    @ExceptionHandler(ConflitoDeHorarioException.class)
    ProblemDetail conflito(ConflitoDeHorarioException erro) {
        return problema(HttpStatus.CONFLICT, "Horário indisponível",
                "Um dos recursos já está reservado nesse horário. Escolha outro horário ou outro recurso.");
    }

    @ExceptionHandler({ OperacaoProibidaException.class, EmprestimoInvalidoException.class })
    ProblemDetail operacaoProibida(RuntimeException erro) {
        return problema(HttpStatus.CONFLICT, "Operação não permitida", erro.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail naoEncontrado(NoSuchElementException erro) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", erro.getMessage());
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
