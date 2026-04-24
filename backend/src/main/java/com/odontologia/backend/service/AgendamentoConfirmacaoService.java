package com.odontologia.backend.service;

import com.odontologia.backend.dto.ConfirmacaoWhatsappDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.entity.NotificacaoWhatsappEntity;
import com.odontologia.backend.repository.AgendamentoRepository;
import com.odontologia.backend.repository.NotificacaoWhatsappRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoConfirmacaoService {

	private final AgendamentoRepository agendamentoRepository;
	private final NotificacaoWhatsappRepository notificacaoRepository;

	public AgendamentoConfirmacaoService(AgendamentoRepository agendamentoRepository,
			NotificacaoWhatsappRepository notificacaoRepository) {
		this.agendamentoRepository = agendamentoRepository;
		this.notificacaoRepository = notificacaoRepository;
	}

	@Transactional
	public AgendamentoEntity processarResposta(ConfirmacaoWhatsappDTO dto) {
		AgendamentoEntity agendamento = agendamentoRepository.findById(dto.getAgendamentoId()).orElseThrow();

		String resposta = dto.getResposta() == null ? "" : dto.getResposta().trim().toUpperCase();

		if ("1".equals(resposta) || "CONFIRMAR".equals(resposta) || "CONFIRMADO".equals(resposta)) {
			agendamento.setStatus("CONFIRMADO");
			agendamento.setConfirmadoWhatsapp(true);
			agendamento.setDataConfirmacaoWhatsapp(LocalDateTime.now());
		} else if ("2".equals(resposta) || "CANCELAR".equals(resposta) || "CANCELADO".equals(resposta)) {
			agendamento.setStatus("CANCELADO");
		}

		AgendamentoEntity salvo = agendamentoRepository.save(agendamento);

		List<NotificacaoWhatsappEntity> notificacoes = notificacaoRepository
				.findByAgendamentoId(dto.getAgendamentoId());
		for (NotificacaoWhatsappEntity n : notificacoes) {
			n.setStatus("RESPONDIDO");
			n.setResposta(dto.getResposta());
			notificacaoRepository.save(n);
		}

		return salvo;
	}
}