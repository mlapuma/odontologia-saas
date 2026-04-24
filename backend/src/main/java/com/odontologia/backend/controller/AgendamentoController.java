package com.odontologia.backend.controller;

import com.odontologia.backend.dto.AgendaEventoDTO;
import com.odontologia.backend.dto.AgendamentoDetalheResponseDTO;
import com.odontologia.backend.dto.AgendamentoRequestDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.AgendamentoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

	private final AgendamentoService service;

	public AgendamentoController(AgendamentoService service) {
		this.service = service;
	}

	@PostMapping
	public AgendamentoDetalheResponseDTO criar(@RequestBody AgendamentoRequestDTO dto) {
		return service.criar(dto);
	}

	@GetMapping("/{id}")
	public AgendamentoDetalheResponseDTO detalhar(@PathVariable Long id) {
		return service.detalhar(id);
	}

	@GetMapping("/profissional/{profissionalId}")
	public List<AgendamentoEntity> listarPorProfissional(@RequestParam Long tenantId,
			@PathVariable Long profissionalId) {
		return service.listarPorProfissional(tenantId, profissionalId);
	}

	@GetMapping("/dia")
	public List<AgendamentoEntity> listarPorDia(@RequestParam Long tenantId, @RequestParam String data) {
		return service.listarPorDia(tenantId, LocalDate.parse(data));
	}

	@PostMapping("/{id}/confirmar-whatsapp")
	public AgendamentoEntity confirmarWhatsapp(@PathVariable Long id) {
		return service.confirmarWhatsapp(id);
	}

	@GetMapping("/calendario")
	public List<AgendaEventoDTO> calendario(@RequestParam String inicio, @RequestParam String fim) {
		Long tenantId = TenantContext.getTenantId();
		return service.listarEventosCalendario(tenantId, LocalDateTime.parse(inicio), LocalDateTime.parse(fim));
	}
}