package com.odontologia.backend.repository;

import com.odontologia.backend.entity.NotificacaoWhatsappEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoWhatsappRepository extends JpaRepository<NotificacaoWhatsappEntity, Long> {

	List<NotificacaoWhatsappEntity> findByAgendamentoId(Long agendamentoId);

	List<NotificacaoWhatsappEntity> findByTenantIdAndStatus(Long tenantId, String status);
}