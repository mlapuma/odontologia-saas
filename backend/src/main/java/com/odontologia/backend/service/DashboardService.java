package com.odontologia.backend.service;

import com.odontologia.backend.dto.DashboardResumoDTO;
import com.odontologia.backend.repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

	private final DashboardRepository repository;
	private final GoogleBusinessProfileService googleBusinessProfileService;

	public DashboardService(DashboardRepository repository, GoogleBusinessProfileService googleBusinessProfileService) {
		this.repository = repository;
		this.googleBusinessProfileService = googleBusinessProfileService;
	}

	public DashboardResumoDTO obterResumo(Long tenantId) {

		LocalDate hoje = LocalDate.now();
		LocalDate inicioMes = hoje.withDayOfMonth(1);
		LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
		LocalDate limiteReativacao = hoje.minusDays(180);

		LocalDateTime inicio = hoje.atStartOfDay();
		LocalDateTime fim = hoje.atTime(23, 59, 59);

		DashboardResumoDTO dto = new DashboardResumoDTO();

		dto.setPacientesTotal(repository.totalPacientes(tenantId));
		dto.setPacientesAtivos(repository.pacientesAtivos(tenantId));

		dto.setAgendamentosHoje(repository.agendamentosHoje(tenantId, inicio, fim));

		dto.setConsultasConfirmadasHoje(repository.consultasConfirmadasHoje(tenantId, inicio, fim));

		dto.setConsultasCanceladasHoje(repository.consultasCanceladasHoje(tenantId, inicio, fim));

		dto.setFaturamentoPrevistoHoje(repository.faturamentoPrevistoHoje(tenantId, inicio, fim));
		dto.setMensagensWhatsappHoje(repository.mensagensWhatsappHoje(tenantId, inicio, fim));
		dto.setTratamentosRealizadosMes(repository.tratamentosRealizadosMes(tenantId, inicioMes, fimMes));
		dto.setValorRecebidoMes(repository.valorRecebidoMes(tenantId, inicioMes, fimMes));
		dto.setPacientesSemRetorno(repository.pacientesSemRetorno(tenantId, limiteReativacao));
		dto.setProximosAgendamentos(repository.proximosAgendamentos(tenantId, LocalDateTime.now()));
		dto.setPacientesParaReativar(repository.pacientesParaReativar(tenantId, limiteReativacao));
		dto.setGoogleBusinessProfile(googleBusinessProfileService.obterResumoPerformance());

		return dto;
	}
}
