package com.odontologia.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.odontologia.backend.entity.PacienteEntity;

public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {
	
	List<PacienteEntity> findByTenantId(Long tenantId);

}
