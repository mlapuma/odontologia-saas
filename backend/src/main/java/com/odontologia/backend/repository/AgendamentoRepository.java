package com.odontologia.backend.repository;

import com.odontologia.backend.entity.AgendamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {

	List<AgendamentoEntity> findByTenantIdAndProfissionalIdOrderByDataHoraInicioAsc(Long tenantId, Long profissionalId);

	List<AgendamentoEntity> findByTenantIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(Long tenantId,
			LocalDateTime inicio, LocalDateTime fim);

	@Query("""
			    select a
			    from AgendamentoEntity a
			    where a.tenantId = :tenantId
			      and a.profissionalId = :profissionalId
			      and a.status <> 'CANCELADO'
			      and a.dataHoraInicio < :novoFim
			      and a.dataHoraFim > :novoInicio
			""")
	List<AgendamentoEntity> buscarConflitos(Long tenantId, Long profissionalId, LocalDateTime novoInicio,
			LocalDateTime novoFim);

	List<AgendamentoEntity> findByTenantIdAndProfissionalIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(
			Long tenantId, Long profissionalId, LocalDateTime inicio, LocalDateTime fim);
}