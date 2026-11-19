package br.senac.alocasenac.reserva;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OcupacaoRepository extends JpaRepository<Ocupacao, Long> {

    List<Ocupacao> findByRecursoIdInAndAtivaTrue(Collection<Long> recursoIds);

    List<Ocupacao> findByReferenciaIdAndOrigem(Long referenciaId, OrigemOcupacao origem);

    List<Ocupacao> findByAtivaTrue();
}
