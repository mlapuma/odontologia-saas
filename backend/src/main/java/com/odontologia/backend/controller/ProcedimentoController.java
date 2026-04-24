package com.odontologia.backend.controller;

import com.odontologia.backend.entity.ProcedimentoEntity;
import com.odontologia.backend.repository.ProcedimentoRepository;
import com.odontologia.backend.security.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/procedimentos")
public class ProcedimentoController {

	private final ProcedimentoRepository repository;

	public ProcedimentoController(ProcedimentoRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<ProcedimentoEntity> listar() {
		return repository.findByTenantId(TenantContext.getTenantId());
	}
}
