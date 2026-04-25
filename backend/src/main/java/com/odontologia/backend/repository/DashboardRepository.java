package com.odontologia.backend.repository;

import org.springframework.stereotype.Repository;

import com.odontologia.backend.dto.DashboardResumoDTO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class DashboardRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public Long totalPacientes(Long tenantId) {
		return entityManager.createQuery("""
				select count(p)
				from PacienteEntity p
				where p.tenantId = :tenantId
				""", Long.class).setParameter("tenantId", tenantId).getSingleResult();
	}

	public Long pacientesAtivos(Long tenantId) {
		return entityManager.createQuery("""
				select count(p)
				from PacienteEntity p
				where p.tenantId = :tenantId
				and p.ativo = true
				""", Long.class).setParameter("tenantId", tenantId).getSingleResult();
	}

	public Long agendamentosHoje(Long tenantId, LocalDateTime inicio, LocalDateTime fim) {

		return entityManager.createQuery("""
				select count(a)
				from AgendamentoEntity a
				where a.tenantId = :tenantId
				and a.dataHoraInicio between :inicio and :fim
				""", Long.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();
	}

	public Long consultasConfirmadasHoje(Long tenantId, LocalDateTime inicio, LocalDateTime fim) {

		return entityManager.createQuery("""
				select count(a)
				from AgendamentoEntity a
				where a.tenantId = :tenantId
				and a.status = 'CONFIRMADO'
				and a.dataHoraInicio between :inicio and :fim
				""", Long.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();
	}

	public Long consultasCanceladasHoje(Long tenantId, LocalDateTime inicio, LocalDateTime fim) {

		return entityManager.createQuery("""
				select count(a)
				from AgendamentoEntity a
				where a.tenantId = :tenantId
				and a.status = 'CANCELADO'
				and a.dataHoraInicio between :inicio and :fim
				""", Long.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();
	}

	public BigDecimal faturamentoPrevistoHoje(Long tenantId, LocalDateTime inicio, LocalDateTime fim) {

		BigDecimal valor = entityManager.createQuery("""
				select sum(ap.valorCobrado * ap.quantidade)
				from AgendamentoProcedimentoEntity ap
				join AgendamentoEntity a on a.id = ap.agendamentoId
				where a.tenantId = :tenantId
				and a.dataHoraInicio between :inicio and :fim
				""", BigDecimal.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();

		return valor == null ? BigDecimal.ZERO : valor;
	}

	public Long mensagensWhatsappHoje(Long tenantId, LocalDateTime inicio, LocalDateTime fim) {
		return entityManager.createQuery("""
				select count(n)
				from NotificacaoWhatsappEntity n
				where n.tenantId = :tenantId
				and n.status = 'ENVIADO'
				and n.dataEnvio between :inicio and :fim
				""", Long.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();
	}

	public Long tratamentosRealizadosMes(Long tenantId, LocalDate inicio, LocalDate fim) {
		return entityManager.createQuery("""
				select count(t)
				from TratamentoRealizadoEntity t
				where t.tenantId = :tenantId
				and t.dataRealizacao between :inicio and :fim
				""", Long.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();
	}

	public BigDecimal valorRecebidoMes(Long tenantId, LocalDate inicio, LocalDate fim) {
		BigDecimal valor = entityManager.createQuery("""
				select sum(t.valorPago)
				from TratamentoRealizadoEntity t
				where t.tenantId = :tenantId
				and t.dataRealizacao between :inicio and :fim
				""", BigDecimal.class).setParameter("tenantId", tenantId).setParameter("inicio", inicio)
				.setParameter("fim", fim).getSingleResult();

		return valor == null ? BigDecimal.ZERO : valor;
	}

	public Long pacientesSemRetorno(Long tenantId, LocalDate limite) {
		int total = entityManager.createQuery("""
				select p.id
				from TratamentoRealizadoEntity t
				join PacienteEntity p on p.id = t.pacienteId and p.tenantId = t.tenantId
				where t.tenantId = :tenantId
				  and p.ativo = true
				  and p.whatsapp is not null
				  and trim(p.whatsapp) <> ''
				group by p.id
				having max(t.dataRealizacao) < :limite
				""", Long.class).setParameter("tenantId", tenantId).setParameter("limite", limite)
				.getResultList().size();
		return (long) total;
	}

	public List<DashboardResumoDTO.ProximoAgendamentoDTO> proximosAgendamentos(Long tenantId,
			LocalDateTime agora) {
		return entityManager.createQuery("""
				select new com.odontologia.backend.dto.DashboardResumoDTO$ProximoAgendamentoDTO(
					a.id,
					p.nome,
					a.dataHoraInicio,
					a.status
				)
				from AgendamentoEntity a
				join PacienteEntity p on p.id = a.pacienteId and p.tenantId = a.tenantId
				where a.tenantId = :tenantId
				  and a.status <> 'CANCELADO'
				  and a.dataHoraInicio >= :agora
				order by a.dataHoraInicio asc
				""", DashboardResumoDTO.ProximoAgendamentoDTO.class).setParameter("tenantId", tenantId)
				.setParameter("agora", agora).setMaxResults(5).getResultList();
	}

	public List<DashboardResumoDTO.PacienteSemRetornoDTO> pacientesParaReativar(Long tenantId, LocalDate limite) {
		return entityManager.createQuery("""
				select new com.odontologia.backend.dto.DashboardResumoDTO$PacienteSemRetornoDTO(
					p.id,
					p.nome,
					p.whatsapp,
					max(t.dataRealizacao)
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
				""", DashboardResumoDTO.PacienteSemRetornoDTO.class).setParameter("tenantId", tenantId)
				.setParameter("limite", limite).setMaxResults(5).getResultList();
	}
}
