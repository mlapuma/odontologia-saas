package com.odontologia.backend.repository;

import com.odontologia.backend.entity.ProcedimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedimentoRepository extends JpaRepository<ProcedimentoEntity, Long> {

    List<ProcedimentoEntity> findByTenantId(Long tenantId);

    List<ProcedimentoEntity> findByTenantIdAndNomeIgnoreCaseOrderByAtivoDescIdAsc(Long tenantId, String nome);
}
