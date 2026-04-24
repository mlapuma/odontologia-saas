package com.odontologia.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgendamentoRequestDTO {

	private Long tenantId;
	private Long pacienteId;
	private Long profissionalId;
	private Long tabelaPrecoId;
	private LocalDateTime dataHoraInicio;
	private String observacoes;
	private List<AgendamentoProcedimentoDTO> procedimentos;

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

	public Long getTabelaPrecoId() {
		return tabelaPrecoId;
	}

	public void setTabelaPrecoId(Long tabelaPrecoId) {
		this.tabelaPrecoId = tabelaPrecoId;
	}

	public LocalDateTime getDataHoraInicio() {
		return dataHoraInicio;
	}

	public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
		this.dataHoraInicio = dataHoraInicio;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public List<AgendamentoProcedimentoDTO> getProcedimentos() {
		return procedimentos;
	}

	public void setProcedimentos(List<AgendamentoProcedimentoDTO> procedimentos) {
		this.procedimentos = procedimentos;
	}
}