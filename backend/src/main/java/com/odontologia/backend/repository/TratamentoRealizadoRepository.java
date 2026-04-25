package com.odontologia.backend.repository;

import com.odontologia.backend.dto.PacienteReativacaoDTO;
import com.odontologia.backend.entity.TratamentoRealizadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TratamentoRealizadoRepository extends JpaRepository<TratamentoRealizadoEntity, Long> {

	List<TratamentoRealizadoEntity> findByTenantIdOrderByDataRealizacaoDescCreatedAtDesc(Long tenantId);

	List<TratamentoRealizadoEntity> findByTenantIdAndPacienteIdOrderByDataRealizacaoDescCreatedAtDesc(Long tenantId,
			Long pacienteId);

	@Query("""
			select new com.odontologia.backend.dto.PacienteReativacaoDTO(
				p.id,
				p.nome,
				p.whatsapp,
				max(t.dataRealizacao),
				sum(t.valorPago)
			)
			from TratamentoRealizadoEntity t
			join PacienteEntity p on p.id = t.pacienteId and p.tenantId = t.tenantId
			where t.tenantId = :tenantId
			  and p.ativo = true
			  and p.whatsapp is not null
			  and trim(p.whatsapp) <> ''
			group by p.id, p.nome, p.whatsapp
			having max(t.dataRealizacao) < :limite
			order by max(t.dataRealizacao) asc
			""")
	List<PacienteReativacaoDTO> buscarPacientesParaReativacao(@Param("tenantId") Long tenantId,
			@Param("limite") LocalDate limite);
}
