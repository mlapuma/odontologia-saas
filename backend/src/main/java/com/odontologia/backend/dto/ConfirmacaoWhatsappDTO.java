package com.odontologia.backend.dto;

public class ConfirmacaoWhatsappDTO {

	private Long agendamentoId;
	private String resposta;

	public Long getAgendamentoId() {
		return agendamentoId;
	}

	public void setAgendamentoId(Long agendamentoId) {
		this.agendamentoId = agendamentoId;
	}

	public String getResposta() {
		return resposta;
	}

	public void setResposta(String resposta) {
		this.resposta = resposta;
	}
}