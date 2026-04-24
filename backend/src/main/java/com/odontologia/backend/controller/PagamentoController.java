package com.odontologia.backend.controller;

import com.odontologia.backend.dto.FinanceiroResumoDTO;
import com.odontologia.backend.dto.PagamentoRequestDTO;
import com.odontologia.backend.dto.RecebimentoDTO;
import com.odontologia.backend.entity.PagamentoEntity;
import com.odontologia.backend.service.FinanceiroResumoService;
import com.odontologia.backend.service.PagamentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

	private final PagamentoService pagamentoService;
	private final FinanceiroResumoService financeiroResumoService;

	public PagamentoController(PagamentoService pagamentoService, FinanceiroResumoService financeiroResumoService) {
		this.pagamentoService = pagamentoService;
		this.financeiroResumoService = financeiroResumoService;
	}

	@PostMapping("/gerar")
	public PagamentoEntity gerar(@RequestBody PagamentoRequestDTO dto) {
		return pagamentoService.gerarCobranca(dto);
	}

	@GetMapping
	public List<PagamentoEntity> listar(@RequestParam Long tenantId) {
		return pagamentoService.listar(tenantId);
	}

	@GetMapping("/pendentes")
	public List<PagamentoEntity> listarPendentes(@RequestParam Long tenantId) {
		return pagamentoService.listarPendentes(tenantId);
	}

	@PostMapping("/{id}/receber")
	public PagamentoEntity receber(@PathVariable Long id, @RequestBody RecebimentoDTO dto) {
		return pagamentoService.receber(id, dto);
	}

	@GetMapping("/resumo")
	public FinanceiroResumoDTO resumo(@RequestParam Long tenantId) {
		return financeiroResumoService.resumo(tenantId);
	}
}