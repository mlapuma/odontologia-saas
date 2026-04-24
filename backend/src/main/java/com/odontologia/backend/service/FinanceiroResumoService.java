package com.odontologia.backend.service;

import com.odontologia.backend.dto.FinanceiroResumoDTO;
import com.odontologia.backend.entity.PagamentoEntity;
import com.odontologia.backend.repository.PagamentoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FinanceiroResumoService {

	private final PagamentoRepository repository;

	public FinanceiroResumoService(PagamentoRepository repository) {
		this.repository = repository;
	}

	public FinanceiroResumoDTO resumo(Long tenantId) {
		FinanceiroResumoDTO dto = new FinanceiroResumoDTO();

		LocalDate hoje = LocalDate.now();
		LocalDateTime inicio = hoje.atStartOfDay();
		LocalDateTime fim = hoje.atTime(23, 59, 59);

		List<PagamentoEntity> pagosHoje = repository.findByTenantIdAndDataPagamentoBetween(tenantId, inicio, fim);
		List<PagamentoEntity> pendentes = repository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, "PENDENTE");

		BigDecimal totalRecebidoHoje = BigDecimal.ZERO;
		for (PagamentoEntity p : pagosHoje) {
			if (p.getValorPago() != null) {
				totalRecebidoHoje = totalRecebidoHoje.add(p.getValorPago());
			}
		}

		BigDecimal totalPendente = BigDecimal.ZERO;
		for (PagamentoEntity p : pendentes) {
			if (p.getValorTotal() != null && p.getValorPago() != null) {
				totalPendente = totalPendente.add(p.getValorTotal().subtract(p.getValorPago()));
			}
		}

		dto.setTotalRecebidoHoje(totalRecebidoHoje);
		dto.setTotalPendente(totalPendente);
		dto.setQuantidadePendencias((long) pendentes.size());
		dto.setQuantidadePagosHoje((long) pagosHoje.size());

		return dto;
	}
}