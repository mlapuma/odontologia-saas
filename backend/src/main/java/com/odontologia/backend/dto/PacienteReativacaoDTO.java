package com.odontologia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PacienteReativacaoDTO {

	private Long pacienteId;
	private String nome;
	private String whatsapp;
	private LocalDate ultimaDataTratamento;
	private BigDecimal totalPago;

	public PacienteReativacaoDTO(Long pacienteId, String nome, String whatsapp, LocalDate ultimaDataTratamento,
			BigDecimal totalPago) {
		this.pacienteId = pacienteId;
		this.nome = nome;
		this.whatsapp = whatsapp;
		this.ultimaDataTratamento = ultimaDataTratamento;
		this.totalPago = totalPago;
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

	public BigDecimal getTotalPago() {
		return totalPago;
	}
}
