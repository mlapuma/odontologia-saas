package com.odontologia.backend.scheduler;

import com.odontologia.backend.service.WhatsappCampanhaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WhatsappCampanhaScheduler {

	private final WhatsappCampanhaService service;

	@Value("${app.scheduler.tenant-id:1}")
	private Long tenantId;

	public WhatsappCampanhaScheduler(WhatsappCampanhaService service) {
		this.service = service;
	}

	@Scheduled(cron = "${app.whatsapp.aniversario.cron:0 0 9 * * *}")
	public void enviarAniversariantes() {
		service.enviarAniversariantesDoDia(tenantId);
	}

	@Scheduled(cron = "${app.whatsapp.promocao.cron:0 30 10 * * *}")
	public void enviarPromocoesReativacao() {
		service.enviarPromocaoPacientesInativos(tenantId);
	}
}
