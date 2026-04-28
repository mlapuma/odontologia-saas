package com.odontologia.backend.controller;

import com.odontologia.backend.dto.PacienteReativacaoDTO;
import com.odontologia.backend.dto.TratamentoRealizadoRequestDTO;
import com.odontologia.backend.entity.TratamentoRealizadoEntity;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.TratamentoRealizadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamentos-realizados")
public class TratamentoRealizadoController {

	private final TratamentoRealizadoService service;

	public TratamentoRealizadoController(TratamentoRealizadoService service) {
		this.service = service;
	}

	@GetMapping
	public List<TratamentoRealizadoEntity> listar(@RequestParam(required = false) Long pacienteId) {
		return service.listar(TenantContext.getTenantId(), pacienteId);
	}

	@PostMapping
	public TratamentoRealizadoEntity criar(@RequestBody TratamentoRealizadoRequestDTO dto) {
		return service.criar(TenantContext.getTenantId(), dto);
	}

	@PutMapping("/{id}")
	public TratamentoRealizadoEntity atualizar(@PathVariable Long id, @RequestBody TratamentoRealizadoRequestDTO dto) {
		return service.atualizar(TenantContext.getTenantId(), id, dto);
	}

	@DeleteMapping("/{id}")
	public void excluir(@PathVariable Long id) {
		service.excluir(TenantContext.getTenantId(), id);
	}

	@PostMapping("/{id}/finalizar")
	public TratamentoRealizadoEntity finalizar(@PathVariable Long id) {
		return service.finalizar(TenantContext.getTenantId(), id);
	}

	@GetMapping("/pacientes-reativacao")
	public List<PacienteReativacaoDTO> pacientesParaReativacao(
			@RequestParam(defaultValue = "180") int diasSemComparecer) {
		return service.pacientesParaReativacao(TenantContext.getTenantId(), diasSemComparecer);
	}
}
