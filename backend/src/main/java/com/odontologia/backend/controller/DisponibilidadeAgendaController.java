package com.odontologia.backend.controller;

import com.odontologia.backend.dto.HorarioDisponivelDTO;
import com.odontologia.backend.service.DisponibilidadeAgendaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos/disponibilidade")
public class DisponibilidadeAgendaController {

	private final DisponibilidadeAgendaService service;

	public DisponibilidadeAgendaController(DisponibilidadeAgendaService service) {
		this.service = service;
	}

	@GetMapping
	public List<HorarioDisponivelDTO> buscarDisponibilidade(@RequestParam Long tenantId,
			@RequestParam Long profissionalId, @RequestParam String data, @RequestParam Integer duracaoMinutos) {
		return service.buscarHorariosDisponiveis(tenantId, profissionalId, LocalDate.parse(data), duracaoMinutos);
	}
}