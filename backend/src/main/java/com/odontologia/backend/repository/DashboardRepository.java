package com.odontologia.backend.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}