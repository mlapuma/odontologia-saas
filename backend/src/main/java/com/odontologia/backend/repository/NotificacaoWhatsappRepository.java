package com.odontologia.backend.repository;

import com.odontologia.backend.entity.NotificacaoWhatsappEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacaoWhatsappRepository extends JpaRepository<NotificacaoWhatsappEntity, Long> {

	List<NotificacaoWhatsappEntity> findByAgendamentoId(Long agendamentoId);

	List<NotificacaoWhatsappEntity> findByTenantIdAndStatus(Long tenantId, String status);

	boolean existsByTenantIdAndAgendamentoIdAndTipo(Long tenantId, Long agendamentoId, String tipo);

	boolean existsByTenantIdAndPacienteIdAndTipoAndDataEnvioBetween(Long tenantId, Long pacienteId, String tipo,
			LocalDateTime inicio, LocalDateTime fim);
}
