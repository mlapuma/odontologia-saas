package com.odontologia.backend.repository;

import com.odontologia.backend.entity.AgendamentoProcedimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendamentoProcedimentoRepository extends JpaRepository<AgendamentoProcedimentoEntity, Long> {

	List<AgendamentoProcedimentoEntity> findByAgendamentoId(Long agendamentoId);

	void deleteByAgendamentoId(Long agendamentoId);
}