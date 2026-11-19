package br.senac.alocasenac.recurso;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.senac.alocasenac.auditoria.AuditoriaService;

/** Cadastro de recursos (RF2) com auditoria na mesma transação (RF10, ADR-004). */
@Service
public class RecursoService {

    private final RecursoRepository recursos;
    private final AuditoriaService auditoria;

    public RecursoService(RecursoRepository recursos, AuditoriaService auditoria) {
        this.recursos = recursos;
        this.auditoria = auditoria;
    }

    @Transactional
    public Recurso cadastrar(NovoRecursoRequest dados, String ator) {
        Recurso recurso = recursos.save(Recurso.novo(dados.nome(), dados.tipo(), dados.capacidade(),
                dados.localizacao(), dados.restrito()));
        auditoria.registrar("Recurso", recurso.getId(), "CRIACAO", null, recurso.descricao(), ator);
        return recurso;
    }
}
