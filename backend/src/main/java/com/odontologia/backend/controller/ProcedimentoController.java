package com.odontologia.backend.controller;

import com.odontologia.backend.entity.ProcedimentoEntity;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.ProcedimentoPadraoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
