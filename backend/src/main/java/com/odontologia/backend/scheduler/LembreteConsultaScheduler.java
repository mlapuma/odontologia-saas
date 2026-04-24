package com.odontologia.backend.scheduler;

import com.odontologia.backend.service.LembreteConsultaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LembreteConsultaScheduler {

	private final LembreteConsultaService service;

	@Value("${app.scheduler.tenant-id:1}")
	private Long tenantId;

	public LembreteConsultaScheduler(LembreteConsultaService service) {
		this.service = service;
	}

	@Scheduled(fixedDelay = 300000)
	public void executar() {
		service.enviarLembretesProximas24h(tenantId);
	}
}
