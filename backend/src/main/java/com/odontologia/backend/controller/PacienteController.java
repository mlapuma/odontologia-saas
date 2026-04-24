package com.odontologia.backend.controller;

import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.PacienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

	private final PacienteService service;

	public PacienteController(PacienteService service) {
		this.service = service;
	}

	@GetMapping
	public List<PacienteEntity> listar() {
		return service.listar(TenantContext.getTenantId());
	}

	@GetMapping("/{id}")
	public PacienteEntity buscar(@PathVariable Long id) {
		return service.buscar(id);
	}

	@PostMapping
	public PacienteEntity criar(@RequestBody PacienteEntity paciente) {
		paciente.setTenantId(TenantContext.getTenantId());
		return service.salvar(paciente);
	}

	@PutMapping("/{id}")
	public PacienteEntity atualizar(@PathVariable Long id, @RequestBody PacienteEntity paciente) {
		paciente.setTenantId(TenantContext.getTenantId());
		return service.atualizar(id, paciente);
	}

	@DeleteMapping("/{id}")
	public void deletar(@PathVariable Long id) {
		service.deletar(id);
	}
}