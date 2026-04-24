package com.odontologia.backend.dto;

public class FichaPacientePreviewDTO {

	private PacienteFichaDTO paciente;
	private String textoExtraido;
	private String aviso;

	public PacienteFichaDTO getPaciente() {
		return paciente;
	}

	public void setPaciente(PacienteFichaDTO paciente) {
		this.paciente = paciente;
	}

	public String getTextoExtraido() {
		return textoExtraido;
	}

	public void setTextoExtraido(String textoExtraido) {
		this.textoExtraido = textoExtraido;
	}

	public String getAviso() {
		return aviso;
	}

	public void setAviso(String aviso) {
		this.aviso = aviso;
	}
}
