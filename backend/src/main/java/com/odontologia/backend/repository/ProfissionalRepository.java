package com.odontologia.backend.repository;

import com.odontologia.backend.entity.ProfissionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepository extends JpaRepository<ProfissionalEntity, Long> {

	List<ProfissionalEntity> findByTenantIdOrderByNomeAsc(Long tenantId);
}
