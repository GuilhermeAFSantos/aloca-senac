package br.senac.alocasenac.auditoria;

import java.util.List;

import org.springframework.stereotype.Service;

/** Grava auditoria na transação corrente (ADR-004) e consulta o histórico (RF10). */
@Service
public class AuditoriaService {

    public void registrar(String entidade, Long entidadeId, String acao, String valorAnterior, String valorNovo,
            String ator) {
        // assinatura mínima: regra implementada na Aula 06
    }

    public List<RegistroAuditoria> historico(String entidade, Long entidadeId) {
        return List.of();
    }
}
