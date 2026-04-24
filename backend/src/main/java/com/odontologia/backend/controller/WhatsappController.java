package com.odontologia.backend.controller;

import com.odontologia.backend.dto.ConfirmacaoWhatsappDTO;
import com.odontologia.backend.dto.WhatsappRequestDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.entity.NotificacaoWhatsappEntity;
import com.odontologia.backend.service.AgendamentoConfirmacaoService;
import com.odontologia.backend.service.WhatsappService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

	private final WhatsappService whatsappService;
	private final AgendamentoConfirmacaoService confirmacaoService;

	public WhatsappController(WhatsappService whatsappService, AgendamentoConfirmacaoService confirmacaoService) {
		this.whatsappService = whatsappService;
		this.confirmacaoService = confirmacaoService;
	}

	@PostMapping("/enviar")
	public NotificacaoWhatsappEntity enviar(@RequestBody WhatsappRequestDTO dto) {
		return whatsappService.enviar(dto);
	}

	@PostMapping("/resposta")
	public AgendamentoEntity resposta(@RequestBody ConfirmacaoWhatsappDTO dto) {
		return confirmacaoService.processarResposta(dto);
	}
}