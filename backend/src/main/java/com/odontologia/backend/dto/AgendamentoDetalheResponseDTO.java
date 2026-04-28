package com.odontologia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AgendamentoDetalheResponseDTO {

	private Long id;
	private Long tenantId;
	private Long pacienteId;
	private Long profissionalId;
	private LocalDateTime dataHoraInicio;
	private LocalDateTime dataHoraFim;
	private String status;
	private String observacoes;
	private Boolean confirmadoWhatsapp;
	private BigDecimal valorTotal;
	private Integer duracaoTotalMinutos;
	private List<AgendamentoProcedimentoDTO> procedimentos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(Long pacienteId) {
		this.pacienteId = pacienteId;
	}

	public Long getProfissionalId() {
		return profissionalId;
	}

	public void setProfissionalId(Long profissionalId) {
		this.profissionalId = profissionalId;
	}

	public LocalDateTime getDataHoraInicio() {
		return dataHoraInicio;
	}

	public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
		this.dataHoraInicio = dataHoraInicio;
	}

	public LocalDateTime getDataHoraFim() {
		return dataHoraFim;
	}

	public void setDataHoraFim(LocalDateTime dataHoraFim) {
		this.dataHoraFim = dataHoraFim;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public Boolean getConfirmadoWhatsapp() {
		return confirmadoWhatsapp;
	}

	public void setConfirmadoWhatsapp(Boolean confirmadoWhatsapp) {
		this.confirmadoWhatsapp = confirmadoWhatsapp;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public Integer getDuracaoTotalMinutos() {
		return duracaoTotalMinutos;
	}

	public void setDuracaoTotalMinutos(Integer duracaoTotalMinutos) {
		this.duracaoTotalMinutos = duracaoTotalMinutos;
	}

	public List<AgendamentoProcedimentoDTO> getProcedimentos() {
		return procedimentos;
	}

	public void setProcedimentos(List<AgendamentoProcedimentoDTO> procedimentos) {
		this.procedimentos = procedimentos;
	}
}
