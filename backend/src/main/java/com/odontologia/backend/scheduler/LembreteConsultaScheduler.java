package com.odontologia.backend.scheduler;

import com.odontologia.backend.service.LembreteConsultaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LembreteConsultaScheduler {

	private final LembreteConsultaService service;

	public LembreteConsultaScheduler(LembreteConsultaService service) {
		this.service = service;
	}

	@Scheduled(fixedDelay = 300000)
	public void executar() {
		service.enviarLembretesProximas24h(1L);
	}
}