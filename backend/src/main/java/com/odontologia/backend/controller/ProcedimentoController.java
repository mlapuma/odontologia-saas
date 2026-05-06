package com.odontologia.backend.controller;

import com.odontologia.backend.entity.ProcedimentoEntity;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.ProcedimentoPadraoService;
import com.odontologia.backend.service.ProcedimentoPadraoService.ProcedimentoRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/procedimentos")
public class ProcedimentoController {

	private final ProcedimentoPadraoService service;

	public ProcedimentoController(ProcedimentoPadraoService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProcedimentoEntity> listar() {
		return service.listarComPadroes(TenantContext.getTenantId());
	}

	@PutMapping("/{id}/valor")
	public ProcedimentoEntity atualizarValor(@PathVariable Long id, @RequestBody AtualizacaoValorRequest request) {
		return service.atualizarValor(TenantContext.getTenantId(), id, request.valorBase());
	}

	@PostMapping
	public ProcedimentoEntity salvar(@RequestBody ProcedimentoRequest request) {
		return service.salvar(TenantContext.getTenantId(), request);
	}

	@PutMapping("/{id}")
	public ProcedimentoEntity atualizar(@PathVariable Long id, @RequestBody ProcedimentoRequest request) {
		return service.salvar(TenantContext.getTenantId(),
				new ProcedimentoRequest(id, request.nome(), request.valorBase(), request.categoria()));
	}

	public record AtualizacaoValorRequest(BigDecimal valorBase) {
	}
}
