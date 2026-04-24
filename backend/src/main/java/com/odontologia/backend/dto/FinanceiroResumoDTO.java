package com.odontologia.backend.dto;

import java.math.BigDecimal;

public class FinanceiroResumoDTO {

	private BigDecimal totalRecebidoHoje;
	private BigDecimal totalPendente;
	private Long quantidadePendencias;
	private Long quantidadePagosHoje;

	public BigDecimal getTotalRecebidoHoje() {
		return totalRecebidoHoje;
	}

	public void setTotalRecebidoHoje(BigDecimal totalRecebidoHoje) {
		this.totalRecebidoHoje = totalRecebidoHoje;
	}

	public BigDecimal getTotalPendente() {
		return totalPendente;
	}

	public void setTotalPendente(BigDecimal totalPendente) {
		this.totalPendente = totalPendente;
	}

	public Long getQuantidadePendencias() {
		return quantidadePendencias;
	}

	public void setQuantidadePendencias(Long quantidadePendencias) {
		this.quantidadePendencias = quantidadePendencias;
	}

	public Long getQuantidadePagosHoje() {
		return quantidadePagosHoje;
	}

	public void setQuantidadePagosHoje(Long quantidadePagosHoje) {
		this.quantidadePagosHoje = quantidadePagosHoje;
	}
}