package com.odontologia.backend.service;

import com.odontologia.backend.dto.GoogleBusinessProfileConfigDTO;
import com.odontologia.backend.dto.DashboardResumoDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoogleBusinessProfileService {

	private static final Logger logger = LoggerFactory.getLogger(GoogleBusinessProfileService.class);

	private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String ACCOUNT_URL = "https://mybusinessaccountmanagement.googleapis.com/v1/accounts";
	private static final String BUSINESS_INFO_URL = "https://mybusinessbusinessinformation.googleapis.com/v1";
	private static final String PERFORMANCE_URL = "https://businessprofileperformance.googleapis.com/v1";

	private static final List<String> ESCOPOS = List.of(
			"https://www.googleapis.com/auth/business.manage"
	);

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private DashboardResumoDTO.GoogleBusinessProfileDTO resumoCache;
	private LocalDateTime resumoCacheCriadoEm;

	@Value("${google.business-profile.client-id:}")
	private String clientId;

	@Value("${google.business-profile.client-secret:}")
	private String clientSecret;

	@Value("${google.business-profile.redirect-uri:http://localhost:8080/api/integracoes/google-business-profile/callback}")
	private String redirectUri;

	@Value("${google.business-profile.token-file:data/google-business-profile-token.json}")
	private String tokenFile;

	public GoogleBusinessProfileConfigDTO obterStatus() {
		boolean temClientId = possuiValor(clientId);
		boolean temClientSecret = possuiValor(clientSecret);
		boolean configurado = temClientId && temClientSecret;
		boolean autorizado = tokenSalvo();

		GoogleBusinessProfileConfigDTO dto = new GoogleBusinessProfileConfigDTO();
		dto.setClientIdConfigurado(temClientId);
		dto.setClientSecretConfigurado(temClientSecret);
		dto.setConfigurado(configurado);
		dto.setAutorizado(autorizado);
		dto.setRedirectUri(redirectUri);
		dto.setEscopos(ESCOPOS);
		dto.setAuthorizationUrl(configurado ? montarAuthorizationUrl() : null);
		dto.setMensagem(autorizado
				? "Conta Google autorizada. Próximo passo: buscar as métricas do Perfil da Empresa."
				: configurado
				? "Credenciais encontradas. Autorize a conta Google que administra o perfil da clínica."
				: "Informe o Client ID e o Client Secret do Google Cloud para habilitar a conexão.");
		return dto;
	}

	public void processarCallback(String code) {
		if (!possuiValor(code)) {
			throw new IllegalArgumentException("Código de autorização do Google não informado.");
		}

		if (!possuiValor(clientId) || !possuiValor(clientSecret)) {
			throw new IllegalStateException("Credenciais do Google Business Profile não configuradas.");
		}

		GoogleTokenResponse token = trocarCodigoPorToken(code);
		salvarToken(token);
	}

	public DashboardResumoDTO.GoogleBusinessProfileDTO obterResumoPerformance() {
		if (cacheValido()) {
			logger.info("Google Business Profile: retornando resumo em cache. metricasDisponiveis={}, busca={}, maps={}, ligacoes={}, rotas={}, site={}, termos={}",
					resumoCache.isMetricasDisponiveis(), resumoCache.getVisualizacoesBusca(),
					resumoCache.getVisualizacoesMaps(), resumoCache.getCliquesTelefone(),
					resumoCache.getPedidosRota(), resumoCache.getCliquesSite(),
					resumoCache.getTermosPesquisa().size());
			return resumoCache;
		}

		DashboardResumoDTO.GoogleBusinessProfileDTO google = new DashboardResumoDTO.GoogleBusinessProfileDTO();
		GoogleBusinessProfileConfigDTO status = obterStatus();

		google.setConfigurado(status.isConfigurado());
		google.setPeriodo("Últimos 30 dias");

		if (!status.isConfigurado()) {
			google.setMensagem("Conecte o Perfil da Empresa no Google para acompanhar visualizações, ligações, rotas e termos pesquisados.");
			logger.info("Google Business Profile: credenciais OAuth nao configuradas.");
			return google;
		}

		if (!status.isAutorizado()) {
			google.setMensagem("Autorize a conta Google na tela de Integrações para buscar as métricas da clínica.");
			logger.info("Google Business Profile: credenciais configuradas, mas conta ainda nao autorizada.");
			return google;
		}

		try {
			logger.info("Google Business Profile: iniciando consulta de performance.");
			String accessToken = obterAccessToken();
			Optional<GoogleLocation> location = primeiraLocalizacao(accessToken);
			if (location.isEmpty()) {
				google.setMensagem("Nenhum perfil de empresa foi encontrado para a conta Google autorizada.");
				logger.warn("Google Business Profile: nenhuma localizacao encontrada para a conta autorizada.");
				return google;
			}

			GoogleLocation localizacao = location.get();
			google.setLocalizacao(localizacao.title());
			preencherMetricas(google, localizacao.name(), accessToken);
			preencherTermosPesquisa(google, localizacao.name(), accessToken);
			google.setMensagem("Métricas carregadas do Google Business Profile.");
			google.setMetricasDisponiveis(true);
			atualizarCache(google);
			logger.info("Google Business Profile: metricas carregadas. localizacao='{}', busca={}, maps={}, ligacoes={}, rotas={}, site={}, termos={}",
					google.getLocalizacao(), google.getVisualizacoesBusca(), google.getVisualizacoesMaps(),
					google.getCliquesTelefone(), google.getPedidosRota(), google.getCliquesSite(),
					google.getTermosPesquisa().size());
			return google;
		} catch (RuntimeException ex) {
			google.setMensagem(mensagemAmigavelGoogle(ex.getMessage()));
			google.setMetricasDisponiveis(false);
			atualizarCache(google);
			logger.warn("Google Business Profile: falha ao carregar metricas. mensagem='{}'", google.getMensagem());
			return google;
		}
	}

	public String paginaCallbackSucesso() {
		return """
				<!doctype html>
				<html lang="pt-BR">
				<head><meta charset="utf-8"><title>Google conectado</title></head>
				<body style="font-family: Arial, sans-serif; padding: 32px;">
				  <h1>Google Business Profile conectado</h1>
				  <p>A autorização foi registrada. Você já pode voltar ao sistema da clínica.</p>
				  <p><a href="http://127.0.0.1:4200/app/integracoes">Voltar para Integrações</a></p>
				</body>
				</html>
				""";
	}

	public String paginaCallbackErro(String mensagem) {
		return """
				<!doctype html>
				<html lang="pt-BR">
				<head><meta charset="utf-8"><title>Erro ao conectar Google</title></head>
				<body style="font-family: Arial, sans-serif; padding: 32px;">
				  <h1>Não foi possível conectar o Google</h1>
				  <p>%s</p>
				  <p><a href="http://127.0.0.1:4200/app/integracoes">Voltar para Integrações</a></p>
				</body>
				</html>
				""".formatted(mensagem);
	}

	private String montarAuthorizationUrl() {
		return UriComponentsBuilder.fromUriString(AUTH_URL)
				.queryParam("client_id", clientId)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("response_type", "code")
				.queryParam("scope", String.join(" ", ESCOPOS))
				.queryParam("access_type", "offline")
				.queryParam("prompt", "consent")
				.queryParam("include_granted_scopes", "true")
				.build()
				.encode()
				.toUriString();
	}

	private GoogleTokenResponse trocarCodigoPorToken(String code) {
		try {
			String form = formUrlEncoded(parametrosToken(code));
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(TOKEN_URL))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(form))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException("Google retornou erro ao gerar token: " + response.body());
			}

			return objectMapper.readValue(response.body(), GoogleTokenResponse.class);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao ler resposta do Google.", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Conexão com Google interrompida.", e);
		}
	}

	private String obterAccessToken() {
		GoogleTokenResponse token = lerToken();
		if (possuiValor(token.refresh_token)) {
			GoogleTokenResponse atualizado = atualizarAccessToken(token.refresh_token);
			if (!possuiValor(atualizado.refresh_token)) {
				atualizado.refresh_token = token.refresh_token;
			}
			salvarToken(atualizado);
			return atualizado.access_token;
		}

		if (possuiValor(token.access_token)) {
			return token.access_token;
		}

		throw new IllegalStateException("Token do Google não encontrado. Reconecte a conta.");
	}

	private GoogleTokenResponse atualizarAccessToken(String refreshToken) {
		try {
			MultiValueMap<String, String> parametros = new LinkedMultiValueMap<>();
			parametros.add("client_id", clientId);
			parametros.add("client_secret", clientSecret);
			parametros.add("refresh_token", refreshToken);
			parametros.add("grant_type", "refresh_token");

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(TOKEN_URL))
					.header("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(formUrlEncoded(parametros)))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException("Google recusou a renovação do token: " + response.body());
			}

			return objectMapper.readValue(response.body(), GoogleTokenResponse.class);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao ler token renovado do Google.", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Renovação do token Google interrompida.", e);
		}
	}

	private Optional<GoogleLocation> primeiraLocalizacao(String accessToken) {
		JsonNode contas = getJson(ACCOUNT_URL, accessToken);
		JsonNode accounts = contas.path("accounts");
		if (!accounts.isArray() || accounts.isEmpty()) {
			return Optional.empty();
		}

		for (JsonNode account : accounts) {
			String accountName = account.path("name").asText();
			if (!possuiValor(accountName)) {
				continue;
			}

			String url = UriComponentsBuilder.fromUriString(BUSINESS_INFO_URL + "/" + accountName + "/locations")
					.queryParam("readMask", "name,title")
					.queryParam("pageSize", 10)
					.build()
					.encode()
					.toUriString();
			JsonNode locais = getJson(url, accessToken);
			JsonNode locations = locais.path("locations");
			if (locations.isArray() && !locations.isEmpty()) {
				JsonNode location = locations.get(0);
				return Optional.of(new GoogleLocation(location.path("name").asText(), location.path("title").asText()));
			}
		}

		return Optional.empty();
	}

	private void preencherMetricas(DashboardResumoDTO.GoogleBusinessProfileDTO google, String locationName,
			String accessToken) {
		LocalDate fim = LocalDate.now().minusDays(1);
		LocalDate inicio = fim.minusDays(29);
		String url = UriComponentsBuilder.fromUriString(PERFORMANCE_URL + "/" + locationName + ":fetchMultiDailyMetricsTimeSeries")
				.queryParam("dailyMetrics", "BUSINESS_IMPRESSIONS_DESKTOP_SEARCH")
				.queryParam("dailyMetrics", "BUSINESS_IMPRESSIONS_MOBILE_SEARCH")
				.queryParam("dailyMetrics", "BUSINESS_IMPRESSIONS_DESKTOP_MAPS")
				.queryParam("dailyMetrics", "BUSINESS_IMPRESSIONS_MOBILE_MAPS")
				.queryParam("dailyMetrics", "CALL_CLICKS")
				.queryParam("dailyMetrics", "WEBSITE_CLICKS")
				.queryParam("dailyMetrics", "BUSINESS_DIRECTION_REQUESTS")
				.queryParam("dailyRange.start_date.year", inicio.getYear())
				.queryParam("dailyRange.start_date.month", inicio.getMonthValue())
				.queryParam("dailyRange.start_date.day", inicio.getDayOfMonth())
				.queryParam("dailyRange.end_date.year", fim.getYear())
				.queryParam("dailyRange.end_date.month", fim.getMonthValue())
				.queryParam("dailyRange.end_date.day", fim.getDayOfMonth())
				.build()
				.encode()
				.toUriString();

		JsonNode resposta = getJson(url, accessToken);
		long busca = somaMetrica(resposta, "BUSINESS_IMPRESSIONS_DESKTOP_SEARCH")
				+ somaMetrica(resposta, "BUSINESS_IMPRESSIONS_MOBILE_SEARCH");
		long maps = somaMetrica(resposta, "BUSINESS_IMPRESSIONS_DESKTOP_MAPS")
				+ somaMetrica(resposta, "BUSINESS_IMPRESSIONS_MOBILE_MAPS");

		google.setVisualizacoesBusca(busca);
		google.setVisualizacoesMaps(maps);
		google.setCliquesTelefone(somaMetrica(resposta, "CALL_CLICKS"));
		google.setCliquesSite(somaMetrica(resposta, "WEBSITE_CLICKS"));
		google.setPedidosRota(somaMetrica(resposta, "BUSINESS_DIRECTION_REQUESTS"));
	}

	private void preencherTermosPesquisa(DashboardResumoDTO.GoogleBusinessProfileDTO google, String locationName,
			String accessToken) {
		YearMonth mesAtual = YearMonth.now();
		String url = UriComponentsBuilder.fromUriString(PERFORMANCE_URL + "/" + locationName + "/searchkeywords/impressions/monthly")
				.queryParam("monthlyRange.start_month.year", mesAtual.getYear())
				.queryParam("monthlyRange.start_month.month", mesAtual.getMonthValue())
				.queryParam("monthlyRange.end_month.year", mesAtual.getYear())
				.queryParam("monthlyRange.end_month.month", mesAtual.getMonthValue())
				.queryParam("pageSize", 10)
				.build()
				.encode()
				.toUriString();

		JsonNode resposta = getJson(url, accessToken);
		JsonNode termos = resposta.path("searchKeywordsCounts");
		if (!termos.isArray()) {
			return;
		}

		for (JsonNode termo : termos) {
			google.getTermosPesquisa().add(new DashboardResumoDTO.TermoPesquisaDTO(
					termo.path("searchKeyword").asText(),
					termo.path("insightsValue").path("value").asLong(0L)));
		}
	}

	private JsonNode getJson(String url, String accessToken) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.header("Authorization", "Bearer " + accessToken)
					.GET()
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException(response.body());
			}

			return objectMapper.readTree(response.body());
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao ler resposta do Google.", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Consulta ao Google interrompida.", e);
		}
	}

	private long somaMetrica(JsonNode resposta, String nomeMetrica) {
		long total = 0L;
		JsonNode series = resposta.path("multiDailyMetricTimeSeries");
		if (!series.isArray()) {
			return total;
		}

		for (JsonNode grupo : series) {
			JsonNode metricas = grupo.path("dailyMetricTimeSeries");
			if (!metricas.isArray()) {
				continue;
			}

			for (JsonNode metrica : metricas) {
				if (!nomeMetrica.equals(metrica.path("dailyMetric").asText())) {
					continue;
				}

				JsonNode valores = metrica.path("timeSeries").path("datedValues");
				if (!valores.isArray()) {
					continue;
				}

				for (JsonNode valor : valores) {
					total += valor.path("value").asLong(0L);
				}
			}
		}

		return total;
	}

	private MultiValueMap<String, String> parametrosToken(String code) {
		MultiValueMap<String, String> parametros = new LinkedMultiValueMap<>();
		parametros.add("code", code);
		parametros.add("client_id", clientId);
		parametros.add("client_secret", clientSecret);
		parametros.add("redirect_uri", redirectUri);
		parametros.add("grant_type", "authorization_code");
		return parametros;
	}

	private String formUrlEncoded(MultiValueMap<String, String> parametros) {
		return parametros.entrySet().stream()
				.flatMap(entry -> entry.getValue().stream()
						.map(value -> encode(entry.getKey()) + "=" + encode(value)))
				.collect(Collectors.joining("&"));
	}

	private String encode(String valor) {
		return URLEncoder.encode(valor, StandardCharsets.UTF_8);
	}

	private void salvarToken(GoogleTokenResponse token) {
		try {
			Path path = Path.of(tokenFile);
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), token);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao salvar token do Google.", e);
		}
	}

	private boolean tokenSalvo() {
		return Files.exists(Path.of(tokenFile));
	}

	private boolean cacheValido() {
		return resumoCache != null
				&& resumoCacheCriadoEm != null
				&& resumoCacheCriadoEm.plusMinutes(15).isAfter(LocalDateTime.now());
	}

	private void atualizarCache(DashboardResumoDTO.GoogleBusinessProfileDTO google) {
		this.resumoCache = google;
		this.resumoCacheCriadoEm = LocalDateTime.now();
	}

	private String mensagemAmigavelGoogle(String mensagemOriginal) {
		if (mensagemOriginal == null) {
			return "Não foi possível carregar as métricas do Google neste momento.";
		}

		if (mensagemOriginal.contains("RATE_LIMIT_EXCEEDED") || mensagemOriginal.contains("Quota exceeded")) {
			return "O Google limitou temporariamente as consultas deste projeto. Aguarde 1 minuto e atualize o Dashboard. O sistema agora usa cache para evitar novas chamadas repetidas.";
		}

		if (mensagemOriginal.contains("SERVICE_DISABLED")) {
			return "Uma API do Google Business Profile ainda não está ativada no Google Cloud. Ative as APIs solicitadas e tente novamente após alguns minutos.";
		}

		if (mensagemOriginal.contains("PERMISSION_DENIED")) {
			return "A conta Google autorizada não tem permissão suficiente para acessar esse Perfil da Empresa.";
		}

		if (mensagemOriginal.contains("ACCESS_TOKEN_SCOPE_INSUFFICIENT")) {
			return "A autorização do Google não possui todas as permissões necessárias. Reconecte a conta Google em Integrações.";
		}

		return "Não foi possível carregar as métricas do Google neste momento. Detalhe técnico: " + mensagemOriginal;
	}

	private GoogleTokenResponse lerToken() {
		try {
			return objectMapper.readValue(Path.of(tokenFile).toFile(), GoogleTokenResponse.class);
		} catch (IOException e) {
			throw new IllegalStateException("Erro ao ler token salvo do Google.", e);
		}
	}

	private boolean possuiValor(String valor) {
		return valor != null && !valor.isBlank();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class GoogleTokenResponse {
		public String access_token;
		public Integer expires_in;
		public String refresh_token;
		public String scope;
		public String token_type;
	}

	private record GoogleLocation(String name, String title) {
	}
}
