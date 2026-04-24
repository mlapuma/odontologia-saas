package com.odontologia.backend.service;

import com.odontologia.backend.dto.DashboardResumoDTO;
import com.odontologia.backend.repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

	private final DashboardRepository repository;

	public DashboardService(DashboardRepository repository) {
		this.repository = repository;
	}

	public DashboardResumoDTO obterResumo(Long tenantId) {

		LocalDate hoje = LocalDate.now();

		LocalDateTime inicio = hoje.atStartOfDay();
		LocalDateTime fim = hoje.atTime(23, 59, 59);

		DashboardResumoDTO dto = new DashboardResumoDTO();

		dto.setPacientesTotal(repository.totalPacientes(tenantId));
		dto.setPacientesAtivos(repository.pacientesAtivos(tenantId));

		dto.setAgendamentosHoje(repository.agendamentosHoje(tenantId, inicio, fim));

		dto.setConsultasConfirmadasHoje(repository.consultasConfirmadasHoje(tenantId, inicio, fim));

		dto.setConsultasCanceladasHoje(repository.consultasCanceladasHoje(tenantId, inicio, fim));

		dto.setFaturamentoPrevistoHoje(repository.faturamentoPrevistoHoje(tenantId, inicio, fim));

		return dto;
	}
}