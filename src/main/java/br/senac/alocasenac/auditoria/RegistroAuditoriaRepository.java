package br.senac.alocasenac.auditoria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    List<RegistroAuditoria> findByEntidadeAndEntidadeIdOrderByInstanteAscIdAsc(String entidade, Long entidadeId);
}
