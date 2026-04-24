package com.odontologia.backend.service;

import com.odontologia.backend.dto.WhatsappRequestDTO;
import com.odontologia.backend.entity.NotificacaoWhatsappEntity;
import com.odontologia.backend.repository.NotificacaoWhatsappRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
public class WhatsappService {

	private final NotificacaoWhatsappRepository repository;
	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${app.whatsapp.enabled:false}")
	private boolean whatsappEnabled;

	@Value("${app.whatsapp.api-url:}")
	private String apiUrl;

	@Value("${app.whatsapp.token:}")
	private String token;

	public WhatsappService(NotificacaoWhatsappRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public NotificacaoWhatsappEntity enviar(WhatsappRequestDTO dto) {
		NotificacaoWhatsappEntity notificacao = new NotificacaoWhatsappEntity();
		notificacao.setTenantId(dto.getTenantId());
		notificacao.setPacienteId(dto.getPacienteId());
		notificacao.setAgendamentoId(dto.getAgendamentoId());
		notificacao.setTipo(dto.getTipo() == null || dto.getTipo().isBlank() ? "CONFIRMACAO" : dto.getTipo());
		notificacao.setTelefoneDestino(dto.getTelefone());
		notificacao.setMensagem(dto.getMensagem());
		notificacao.setStatus("PENDENTE");

		notificacao = repository.save(notificacao);

		try {
			if (whatsappEnabled) {
				enviarParaProvider(dto.getTelefone(), dto.getMensagem());
				notificacao.setResposta("Mensagem enviada pelo provedor configurado");
			} else {
				notificacao.setResposta("Mensagem enviada em modo stub");
			}
			notificacao.setStatus("ENVIADO");
			notificacao.setDataEnvio(LocalDateTime.now());
		} catch (Exception e) {
			notificacao.setStatus("ERRO");
			notificacao.setResposta(e.getMessage());
		}

		return repository.save(notificacao);
	}

	private void enviarParaProvider(String telefone, String mensagem) throws Exception {
		if (apiUrl == null || apiUrl.isBlank()) {
			throw new IllegalStateException("app.whatsapp.api-url nao configurado");
		}

		String body = """
				{"phone":"%s","message":"%s"}
				""".formatted(escaparJson(telefone), escaparJson(mensagem)).trim();

		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(apiUrl))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));

		if (token != null && !token.isBlank()) {
			builder.header("Authorization", "Bearer " + token);
		}

		HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException("Erro do provedor WhatsApp: HTTP " + response.statusCode());
		}
	}

	private String escaparJson(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
	}
}
