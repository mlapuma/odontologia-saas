package com.odontologia.backend.repository;

import com.odontologia.backend.entity.ProcedimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcedimentoRepository extends JpaRepository<ProcedimentoEntity, Long> {

    List<ProcedimentoEntity> findByTenantId(Long tenantId);

    Optional<ProcedimentoEntity> findByTenantIdAndNomeIgnoreCase(Long tenantId, String nome);
}
