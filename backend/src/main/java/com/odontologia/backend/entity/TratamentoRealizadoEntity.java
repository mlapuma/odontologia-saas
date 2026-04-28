package com.odontologia.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tratamento_realizado")
public class TratamentoRealizadoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;

	@Column(name = "paciente_id", nullable = false)
	private Long pacienteId;

	@Column(name = "procedimento_id")
	private Long procedimentoId;

	@Column(nullable = false)
	private String tratamento;

	private String dente;

	@Column(name = "valor_pago", nullable = false)
	private BigDecimal valorPago;

	@Column(name = "valor_tratamento", nullable = false)
	private BigDecimal valorTratamento;

	@Column(name = "valor_total", nullable = false)
	private BigDecimal valorTotal;

	private BigDecimal saldo;

	@Column(name = "forma_pagamento")
	private String formaPagamento;

	@Column(name = "parcelas")
	private Integer parcelas;

	@Column(name = "data_realizacao", nullable = false)
	private LocalDate dataRealizacao;

	private Boolean finalizado;

	@Column(name = "data_finalizacao")
	private LocalDateTime dataFinalizacao;

	private String observacoes;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		createdAt = LocalDateTime.now();
		if (dataRealizacao == null) {
			dataRealizacao = LocalDate.now();
		}
		if (finalizado == null) {
			finalizado = false;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
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

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
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

	public Boolean getFinalizado() {
		return finalizado;
	}

	public void setFinalizado(Boolean finalizado) {
		this.finalizado = finalizado;
	}

	public LocalDateTime getDataFinalizacao() {
		return dataFinalizacao;
	}

	public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
		this.dataFinalizacao = dataFinalizacao;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
}
