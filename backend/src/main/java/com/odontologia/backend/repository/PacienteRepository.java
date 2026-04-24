package com.odontologia.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.odontologia.backend.entity.PacienteEntity;

public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {
	
	List<PacienteEntity> findByTenantId(Long tenantId);

	@Query("""
			select p
			from PacienteEntity p
			where p.tenantId = :tenantId
			  and p.ativo = true
			  and p.whatsapp is not null
			  and trim(p.whatsapp) <> ''
			  and month(p.dataNascimento) = :mes
			  and day(p.dataNascimento) = :dia
			""")
	List<PacienteEntity> buscarAniversariantes(@Param("tenantId") Long tenantId, @Param("mes") int mes,
			@Param("dia") int dia);

	@Query("""
			select p
			from PacienteEntity p
			where p.tenantId = :tenantId
			  and p.ativo = true
			  and p.whatsapp is not null
			  and trim(p.whatsapp) <> ''
			  and exists (
			    select a.id
			    from AgendamentoEntity a
			    where a.tenantId = :tenantId
			      and a.pacienteId = p.id
			      and a.status = 'REALIZADO'
			      and a.dataHoraInicio < :limite
			  )
			  and not exists (
			    select a2.id
			    from AgendamentoEntity a2
			    where a2.tenantId = :tenantId
			      and a2.pacienteId = p.id
			      and a2.status <> 'CANCELADO'
			      and a2.dataHoraInicio >= :limite
			  )
			""")
	List<PacienteEntity> buscarPacientesSemComparecerDesde(@Param("tenantId") Long tenantId,
			@Param("limite") java.time.LocalDateTime limite);

}
