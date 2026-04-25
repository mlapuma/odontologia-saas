package com.odontologia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardResumoDTO {

	private Long pacientesTotal;
	private Long pacientesAtivos;
	private Long pacientesSemRetorno;

	private Long agendamentosHoje;
	private Long consultasConfirmadasHoje;
	private Long consultasCanceladasHoje;
	private Long mensagensWhatsappHoje;
	private Long tratamentosRealizadosMes;

	private BigDecimal faturamentoPrevistoHoje;
	private BigDecimal valorRecebidoMes;

	private List<ProximoAgendamentoDTO> proximosAgendamentos = new ArrayList<>();
	private List<PacienteSemRetornoDTO> pacientesParaReativar = new ArrayList<>();

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

	public Long getPacientesSemRetorno() {
		return pacientesSemRetorno;
	}

	public void setPacientesSemRetorno(Long pacientesSemRetorno) {
		this.pacientesSemRetorno = pacientesSemRetorno;
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

	public Long getMensagensWhatsappHoje() {
		return mensagensWhatsappHoje;
	}

	public void setMensagensWhatsappHoje(Long mensagensWhatsappHoje) {
		this.mensagensWhatsappHoje = mensagensWhatsappHoje;
	}

	public Long getTratamentosRealizadosMes() {
		return tratamentosRealizadosMes;
	}

	public void setTratamentosRealizadosMes(Long tratamentosRealizadosMes) {
		this.tratamentosRealizadosMes = tratamentosRealizadosMes;
	}

	public BigDecimal getFaturamentoPrevistoHoje() {
		return faturamentoPrevistoHoje;
	}

	public void setFaturamentoPrevistoHoje(BigDecimal faturamentoPrevistoHoje) {
		this.faturamentoPrevistoHoje = faturamentoPrevistoHoje;
	}

	public BigDecimal getValorRecebidoMes() {
		return valorRecebidoMes;
	}

	public void setValorRecebidoMes(BigDecimal valorRecebidoMes) {
		this.valorRecebidoMes = valorRecebidoMes;
	}

	public List<ProximoAgendamentoDTO> getProximosAgendamentos() {
		return proximosAgendamentos;
	}

	public void setProximosAgendamentos(List<ProximoAgendamentoDTO> proximosAgendamentos) {
		this.proximosAgendamentos = proximosAgendamentos;
	}

	public List<PacienteSemRetornoDTO> getPacientesParaReativar() {
		return pacientesParaReativar;
	}

	public void setPacientesParaReativar(List<PacienteSemRetornoDTO> pacientesParaReativar) {
		this.pacientesParaReativar = pacientesParaReativar;
	}

	public static class ProximoAgendamentoDTO {
		private Long id;
		private String pacienteNome;
		private LocalDateTime dataHoraInicio;
		private String status;

		public ProximoAgendamentoDTO(Long id, String pacienteNome, LocalDateTime dataHoraInicio, String status) {
			this.id = id;
			this.pacienteNome = pacienteNome;
			this.dataHoraInicio = dataHoraInicio;
			this.status = status;
		}

		public Long getId() {
			return id;
		}

		public String getPacienteNome() {
			return pacienteNome;
		}

		public LocalDateTime getDataHoraInicio() {
			return dataHoraInicio;
		}

		public String getStatus() {
			return status;
		}
	}

	public static class PacienteSemRetornoDTO {
		private Long pacienteId;
		private String nome;
		private String whatsapp;
		private LocalDate ultimaDataTratamento;

		public PacienteSemRetornoDTO(Long pacienteId, String nome, String whatsapp, LocalDate ultimaDataTratamento) {
			this.pacienteId = pacienteId;
			this.nome = nome;
			this.whatsapp = whatsapp;
			this.ultimaDataTratamento = ultimaDataTratamento;
		}

		public Long getPacienteId() {
			return pacienteId;
		}

		public String getNome() {
			return nome;
		}

		public String getWhatsapp() {
			return whatsapp;
		}

		public LocalDate getUltimaDataTratamento() {
			return ultimaDataTratamento;
		}
	}
}
