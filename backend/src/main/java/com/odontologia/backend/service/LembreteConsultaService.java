package com.odontologia.backend.service;

import com.odontologia.backend.dto.WhatsappRequestDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.repository.AgendamentoRepository;
import com.odontologia.backend.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LembreteConsultaService {

	private final AgendamentoRepository agendamentoRepository;
	private final PacienteRepository pacienteRepository;
	private final WhatsappService whatsappService;

	public LembreteConsultaService(AgendamentoRepository agendamentoRepository, PacienteRepository pacienteRepository,
			WhatsappService whatsappService) {
		this.agendamentoRepository = agendamentoRepository;
		this.pacienteRepository = pacienteRepository;
		this.whatsappService = whatsappService;
	}

	public void enviarLembretesProximas24h(Long tenantId) {
		LocalDateTime agora = LocalDateTime.now();
		LocalDateTime limite = agora.plusHours(24);

		List<AgendamentoEntity> agendamentos = agendamentoRepository
				.findByTenantIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(tenantId, agora, limite);

		for (AgendamentoEntity agendamento : agendamentos) {
			if ("CANCELADO".equalsIgnoreCase(agendamento.getStatus())
					|| "CONFIRMADO".equalsIgnoreCase(agendamento.getStatus())) {
				continue;
			}

			PacienteEntity paciente = pacienteRepository.findById(agendamento.getPacienteId()).orElseThrow();

			String dataHora = agendamento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

			String mensagem = """
					Olá %s 👋

					Sua consulta está agendada para %s.

					Responda:
					1 - Confirmar
					2 - Cancelar
					""".formatted(paciente.getNome(), dataHora);

			WhatsappRequestDTO dto = new WhatsappRequestDTO();
			dto.setTenantId(tenantId);
			dto.setPacienteId(paciente.getId());
			dto.setAgendamentoId(agendamento.getId());
			dto.setTelefone(paciente.getWhatsapp());
			dto.setMensagem(mensagem);

			whatsappService.enviar(dto);
		}
	}
}