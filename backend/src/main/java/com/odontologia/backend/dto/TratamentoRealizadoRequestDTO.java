package com.odontologia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TratamentoRealizadoRequestDTO {

	private Long pacienteId;
	private Long procedimentoId;
	private String tratamento;
	private BigDecimal valorPago;
	private LocalDate dataRealizacao;
	private String observacoes;

	public Long getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(Long pacienteId) {
		this.pacienteId = pacienteId;
	}

	public Long getProcedimentoId() {
		return procedimentoId;
	}

	public void setProcedimentoId(Long procedimentoId) {
		this.procedimentoId = procedimentoId;
	}

	public String getTratamento() {
		return tratamento;
	}

	public void setTratamento(String tratamento) {
		this.tratamento = tratamento;
	}

	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}

	public LocalDate getDataRealizacao() {
		return dataRealizacao;
	}

	public void setDataRealizacao(LocalDate dataRealizacao) {
		this.dataRealizacao = dataRealizacao;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
}
