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
	private GoogleBusinessProfileDTO googleBusinessProfile = new GoogleBusinessProfileDTO();

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

	public GoogleBusinessProfileDTO getGoogleBusinessProfile() {
		return googleBusinessProfile;
	}

	public void setGoogleBusinessProfile(GoogleBusinessProfileDTO googleBusinessProfile) {
		this.googleBusinessProfile = googleBusinessProfile;
	}

	public static class ProximoAgendamentoDTO {
		private Long id;
		private Long pacienteId;
		private String pacienteNome;
		private String pacienteWhatsapp;
		private Long profissionalId;
		private String profissionalNome;
		private String tratamento;
		private LocalDateTime dataHoraInicio;
		private String status;

		public ProximoAgendamentoDTO(Long id, Long pacienteId, String pacienteNome, String pacienteWhatsapp,
				Long profissionalId, String profissionalNome, String tratamento, LocalDateTime dataHoraInicio,
				String status) {
			this.id = id;
			this.pacienteId = pacienteId;
			this.pacienteNome = pacienteNome;
			this.pacienteWhatsapp = pacienteWhatsapp;
			this.profissionalId = profissionalId;
			this.profissionalNome = profissionalNome;
			this.tratamento = tratamento;
			this.dataHoraInicio = dataHoraInicio;
			this.status = status;
		}

		public Long getId() {
			return id;
		}

		public Long getPacienteId() {
			return pacienteId;
		}

		public String getPacienteNome() {
			return pacienteNome;
		}

		public String getPacienteWhatsapp() {
			return pacienteWhatsapp;
		}

		public Long getProfissionalId() {
			return profissionalId;
		}

		public String getProfissionalNome() {
			return profissionalNome;
		}

		public String getTratamento() {
			return tratamento;
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

	public static class GoogleBusinessProfileDTO {
		private boolean configurado;
		private Long visualizacoesBusca = 0L;
		private Long visualizacoesMaps = 0L;
		private Long cliquesTelefone = 0L;
		private Long cliquesSite = 0L;
		private Long pedidosRota = 0L;
		private String periodo = "Mês atual";
		private String localizacao;
		private String mensagem = "Integração com o Perfil da Empresa no Google ainda não configurada.";
		private boolean metricasDisponiveis;
		private List<TermoPesquisaDTO> termosPesquisa = new ArrayList<>();

		public boolean isConfigurado() {
			return configurado;
		}

		public void setConfigurado(boolean configurado) {
			this.configurado = configurado;
		}

		public Long getVisualizacoesBusca() {
			return visualizacoesBusca;
		}

		public void setVisualizacoesBusca(Long visualizacoesBusca) {
			this.visualizacoesBusca = visualizacoesBusca;
		}

		public Long getVisualizacoesMaps() {
			return visualizacoesMaps;
		}

		public void setVisualizacoesMaps(Long visualizacoesMaps) {
			this.visualizacoesMaps = visualizacoesMaps;
		}

		public Long getCliquesTelefone() {
			return cliquesTelefone;
		}

		public void setCliquesTelefone(Long cliquesTelefone) {
			this.cliquesTelefone = cliquesTelefone;
		}

		public Long getCliquesSite() {
			return cliquesSite;
		}

		public void setCliquesSite(Long cliquesSite) {
			this.cliquesSite = cliquesSite;
		}

		public Long getPedidosRota() {
			return pedidosRota;
		}

		public void setPedidosRota(Long pedidosRota) {
			this.pedidosRota = pedidosRota;
		}

		public String getPeriodo() {
			return periodo;
		}

		public void setPeriodo(String periodo) {
			this.periodo = periodo;
		}

		public String getLocalizacao() {
			return localizacao;
		}

		public void setLocalizacao(String localizacao) {
			this.localizacao = localizacao;
		}

		public String getMensagem() {
			return mensagem;
		}

		public void setMensagem(String mensagem) {
			this.mensagem = mensagem;
		}

		public boolean isMetricasDisponiveis() {
			return metricasDisponiveis;
		}

		public void setMetricasDisponiveis(boolean metricasDisponiveis) {
			this.metricasDisponiveis = metricasDisponiveis;
		}

		public List<TermoPesquisaDTO> getTermosPesquisa() {
			return termosPesquisa;
		}

		public void setTermosPesquisa(List<TermoPesquisaDTO> termosPesquisa) {
			this.termosPesquisa = termosPesquisa;
		}
	}

	public static class TermoPesquisaDTO {
		private String termo;
		private Long impressoes;

		public TermoPesquisaDTO(String termo, Long impressoes) {
			this.termo = termo;
			this.impressoes = impressoes;
		}

		public String getTermo() {
			return termo;
		}

		public Long getImpressoes() {
			return impressoes;
		}
	}
}
