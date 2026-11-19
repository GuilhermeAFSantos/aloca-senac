package br.senac.alocasenac.reserva;

/** Ações que mudam o estado de uma reserva (RN-E1, RN-E2). */
public enum AcaoReserva {
    APROVAR, REJEITAR, INICIAR, CONCLUIR, CANCELAR, MARCAR_NAO_COMPARECEU
}
