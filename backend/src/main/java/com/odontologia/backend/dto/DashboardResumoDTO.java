package com.odontologia.backend.dto;

import java.math.BigDecimal;

public class DashboardResumoDTO {

	private Long pacientesTotal;
	private Long pacientesAtivos;

	private Long agendamentosHoje;
	private Long consultasConfirmadasHoje;
	private Long consultasCanceladasHoje;

	private BigDecimal faturamentoPrevistoHoje;

	public Long getPacientesTotal() {
		return pacientesTotal;
	}

	public void setPacientesTotal(Long pacientesTotal) {
		this.pacientesTotal = pacientesTotal;
	}

	public Long getPacientesAtivos() {
		return pacientesAtivos;
	}

	public void setPacientesAtivos(Long pacientesAtivos) {
		this.pacientesAtivos = pacientesAtivos;
	}

	public Long getAgendamentosHoje() {
		return agendamentosHoje;
	}

	public void setAgendamentosHoje(Long agendamentosHoje) {
		this.agendamentosHoje = agendamentosHoje;
	}

	public Long getConsultasConfirmadasHoje() {
		return consultasConfirmadasHoje;
	}

	public void setConsultasConfirmadasHoje(Long consultasConfirmadasHoje) {
		this.consultasConfirmadasHoje = consultasConfirmadasHoje;
	}

	public Long getConsultasCanceladasHoje() {
		return consultasCanceladasHoje;
	}

	public void setConsultasCanceladasHoje(Long consultasCanceladasHoje) {
		this.consultasCanceladasHoje = consultasCanceladasHoje;
	}

	public BigDecimal getFaturamentoPrevistoHoje() {
		return faturamentoPrevistoHoje;
	}

	public void setFaturamentoPrevistoHoje(BigDecimal faturamentoPrevistoHoje) {
		this.faturamentoPrevistoHoje = faturamentoPrevistoHoje;
	}
}