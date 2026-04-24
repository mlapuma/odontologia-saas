package com.odontologia.backend.repository;

import com.odontologia.backend.entity.PagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<PagamentoEntity, Long> {

	List<PagamentoEntity> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

	List<PagamentoEntity> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);

	List<PagamentoEntity> findByTenantIdAndDataPagamentoBetween(Long tenantId, LocalDateTime inicio, LocalDateTime fim);
}