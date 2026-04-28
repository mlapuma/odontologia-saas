package com.odontologia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TratamentoRealizadoRequestDTO {

	private Long pacienteId;
	private Long procedimentoId;
	private String tratamento;
	private String dente;
	private BigDecimal valorPago;
	private BigDecimal valorTratamento;
	private BigDecimal valorTotal;
	private String formaPagamento;
	private Integer parcelas;
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

	public String getDente() {
		return dente;
	}

	public void setDente(String dente) {
		this.dente = dente;
	}

	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}

	public BigDecimal getValorTratamento() {
		return valorTratamento;
	}

	public void setValorTratamento(BigDecimal valorTratamento) {
		this.valorTratamento = valorTratamento;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(String formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public Integer getParcelas() {
		return parcelas;
	}

	public void setParcelas(Integer parcelas) {
		this.parcelas = parcelas;
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
