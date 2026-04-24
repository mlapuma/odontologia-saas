package com.odontologia.backend.service;

import com.odontologia.backend.dto.WhatsappRequestDTO;
import com.odontologia.backend.entity.NotificacaoWhatsappEntity;
import com.odontologia.backend.repository.NotificacaoWhatsappRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WhatsappService {

	private final NotificacaoWhatsappRepository repository;

	public WhatsappService(NotificacaoWhatsappRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public NotificacaoWhatsappEntity enviar(WhatsappRequestDTO dto) {
		NotificacaoWhatsappEntity notificacao = new NotificacaoWhatsappEntity();
		notificacao.setTenantId(dto.getTenantId());
		notificacao.setPacienteId(dto.getPacienteId());
		notificacao.setAgendamentoId(dto.getAgendamentoId());
		notificacao.setTipo("CONFIRMACAO");
		notificacao.setTelefoneDestino(dto.getTelefone());
		notificacao.setMensagem(dto.getMensagem());
		notificacao.setStatus("PENDENTE");

		notificacao = repository.save(notificacao);

		try {
			// Stub de integração real
			// Aqui depois entra Z-API / Evolution / Meta
			notificacao.setStatus("ENVIADO");
			notificacao.setDataEnvio(LocalDateTime.now());
			notificacao.setResposta("Mensagem enviada em modo stub");
		} catch (Exception e) {
			notificacao.setStatus("ERRO");
			notificacao.setResposta(e.getMessage());
		}

		return repository.save(notificacao);
	}
}