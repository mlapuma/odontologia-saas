package com.odontologia.backend.service;

import com.odontologia.backend.dto.WhatsappRequestDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.repository.AgendamentoRepository;
import com.odontologia.backend.repository.NotificacaoWhatsappRepository;
import com.odontologia.backend.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LembreteConsultaService {

	private final AgendamentoRepository agendamentoRepository;
	private final PacienteRepository pacienteRepository;
	private final NotificacaoWhatsappRepository notificacaoRepository;
	private final WhatsappService whatsappService;

	public LembreteConsultaService(AgendamentoRepository agendamentoRepository, PacienteRepository pacienteRepository,
			NotificacaoWhatsappRepository notificacaoRepository, WhatsappService whatsappService) {
		this.agendamentoRepository = agendamentoRepository;
		this.pacienteRepository = pacienteRepository;
		this.notificacaoRepository = notificacaoRepository;
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

			boolean lembreteJaEnviado = notificacaoRepository.existsByTenantIdAndAgendamentoIdAndTipo(tenantId,
					agendamento.getId(), "LEMBRETE_AGENDAMENTO");
			if (lembreteJaEnviado) {
				continue;
			}

			PacienteEntity paciente = pacienteRepository.findById(agendamento.getPacienteId()).orElseThrow();
			if (paciente.getWhatsapp() == null || paciente.getWhatsapp().isBlank()) {
				continue;
			}

			String dataHora = agendamento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

			String mensagem = """
					Ola %s.

					Sua consulta esta agendada para %s.

					Responda:
					1 - Confirmar
					2 - Cancelar
					""".formatted(paciente.getNome(), dataHora);

			WhatsappRequestDTO dto = new WhatsappRequestDTO();
			dto.setTenantId(tenantId);
			dto.setPacienteId(paciente.getId());
			dto.setAgendamentoId(agendamento.getId());
			dto.setTipo("LEMBRETE_AGENDAMENTO");
			dto.setTelefone(paciente.getWhatsapp());
			dto.setMensagem(mensagem);

			whatsappService.enviar(dto);
		}
	}
}
