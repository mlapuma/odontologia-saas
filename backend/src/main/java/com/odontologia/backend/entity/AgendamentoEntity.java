package com.odontologia.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
public class AgendamentoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;

	@Column(name = "paciente_id", nullable = false)
	private Long pacienteId;

	@Column(name = "profissional_id", nullable = false)
	private Long profissionalId;

	@Column(name = "tabela_preco_id")
	private Long tabelaPrecoId;

	@Column(name = "data_hora_inicio", nullable = false)
	private LocalDateTime dataHoraInicio;

	@Column(name = "data_hora_fim", nullable = false)
	private LocalDateTime dataHoraFim;

	@Column(nullable = false)
	private String status;

	private String observacoes;

	@Column(name = "confirmado_whatsapp")
	private Boolean confirmadoWhatsapp;

	@Column(name = "data_confirmacao_whatsapp")
	private LocalDateTime dataConfirmacaoWhatsapp;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		createdAt = LocalDateTime.now();
		if (confirmadoWhatsapp == null) {
			confirmadoWhatsapp = false;
		}
		if (status == null) {
			status = "AGENDADO";
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

	public LocalDateTime getDataConfirmacaoWhatsapp() {
		return dataConfirmacaoWhatsapp;
	}

	public void setDataConfirmacaoWhatsapp(LocalDateTime dataConfirmacaoWhatsapp) {
		this.dataConfirmacaoWhatsapp = dataConfirmacaoWhatsapp;
	}
}