package com.odontologia.backend.service;

import com.odontologia.backend.dto.WhatsappRequestDTO;
import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.repository.NotificacaoWhatsappRepository;
import com.odontologia.backend.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class WhatsappCampanhaService {

	private final PacienteRepository pacienteRepository;
	private final NotificacaoWhatsappRepository notificacaoRepository;
	private final WhatsappService whatsappService;

	@Value("${app.whatsapp.promocao.dias-sem-comparecer:180}")
	private int diasSemComparecer;

	@Value("${app.whatsapp.promocao.intervalo-reenvio-dias:90}")
	private int intervaloReenvioDias;

	public WhatsappCampanhaService(PacienteRepository pacienteRepository,
			NotificacaoWhatsappRepository notificacaoRepository, WhatsappService whatsappService) {
		this.pacienteRepository = pacienteRepository;
		this.notificacaoRepository = notificacaoRepository;
		this.whatsappService = whatsappService;
	}

	public void enviarAniversariantesDoDia(Long tenantId) {
		LocalDate hoje = LocalDate.now();
		List<PacienteEntity> pacientes = pacienteRepository.buscarAniversariantes(tenantId, hoje.getMonthValue(),
				hoje.getDayOfMonth());

		LocalDateTime inicioDia = hoje.atStartOfDay();
		LocalDateTime fimDia = hoje.atTime(LocalTime.MAX);

		for (PacienteEntity paciente : pacientes) {
			boolean jaEnviadoHoje = notificacaoRepository.existsByTenantIdAndPacienteIdAndTipoAndDataEnvioBetween(
					tenantId, paciente.getId(), "ANIVERSARIO", inicioDia, fimDia);
			if (jaEnviadoHoje) {
				continue;
			}

			WhatsappRequestDTO dto = new WhatsappRequestDTO();
			dto.setTenantId(tenantId);
			dto.setPacienteId(paciente.getId());
			dto.setTipo("ANIVERSARIO");
			dto.setTelefone(paciente.getWhatsapp());
			dto.setMensagem(mensagemAniversario(paciente));

			whatsappService.enviar(dto);
		}
	}

	public void enviarPromocaoPacientesInativos(Long tenantId) {
		LocalDateTime limite = LocalDateTime.now().minusDays(diasSemComparecer);
		LocalDateTime inicioJanelaReenvio = LocalDateTime.now().minusDays(intervaloReenvioDias);
		LocalDateTime fimJanelaReenvio = LocalDateTime.now();

		List<PacienteEntity> pacientes = pacienteRepository.buscarPacientesSemComparecerDesde(tenantId, limite,
				limite.toLocalDate());

		for (PacienteEntity paciente : pacientes) {
			boolean jaRecebeuRecentemente = notificacaoRepository
					.existsByTenantIdAndPacienteIdAndTipoAndDataEnvioBetween(tenantId, paciente.getId(),
							"PROMO_REATIVACAO", inicioJanelaReenvio, fimJanelaReenvio);
			if (jaRecebeuRecentemente) {
				continue;
			}

			WhatsappRequestDTO dto = new WhatsappRequestDTO();
			dto.setTenantId(tenantId);
			dto.setPacienteId(paciente.getId());
			dto.setTipo("PROMO_REATIVACAO");
			dto.setTelefone(paciente.getWhatsapp());
			dto.setMensagem(mensagemPromocao(paciente));

			whatsappService.enviar(dto);
		}
	}

	private String mensagemAniversario(PacienteEntity paciente) {
		return """
				Ola %s. Feliz aniversario!

				Toda a equipe da clinica deseja um dia especial, com muitos motivos para sorrir.
				""".formatted(paciente.getNome());
	}

	private String mensagemPromocao(PacienteEntity paciente) {
		return """
				Ola %s. Sentimos sua falta por aqui.

				Que tal agendar uma avaliacao preventiva e cuidar do seu sorriso? Responda esta mensagem para falarmos com voce.
				""".formatted(paciente.getNome());
	}
}
