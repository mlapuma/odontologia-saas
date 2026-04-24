package com.odontologia.backend.dto;

import java.time.LocalDateTime;

public class HorarioDisponivelDTO {

	private LocalDateTime inicio;
	private LocalDateTime fim;
	private Boolean disponivel;

	public HorarioDisponivelDTO() {
	}

	public HorarioDisponivelDTO(LocalDateTime inicio, LocalDateTime fim, Boolean disponivel) {
		this.inicio = inicio;
		this.fim = fim;
		this.disponivel = disponivel;
	}

	public LocalDateTime getInicio() {
		return inicio;
	}

	public void setInicio(LocalDateTime inicio) {
		this.inicio = inicio;
	}

	public LocalDateTime getFim() {
		return fim;
	}

	public void setFim(LocalDateTime fim) {
		this.fim = fim;
	}

	public Boolean getDisponivel() {
		return disponivel;
	}

	public void setDisponivel(Boolean disponivel) {
		this.disponivel = disponivel;
	}
}