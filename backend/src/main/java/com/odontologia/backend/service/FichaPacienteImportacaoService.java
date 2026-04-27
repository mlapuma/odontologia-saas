package com.odontologia.backend.service;

import com.odontologia.backend.dto.FichaPacientePreviewDTO;
import com.odontologia.backend.dto.PacienteFichaDTO;
import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.repository.PacienteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FichaPacienteImportacaoService {

	private static final Pattern CPF_PATTERN = Pattern.compile("\\b\\d{3}[.]?\\d{3}[.]?\\d{3}-?\\d{2}\\b");
	private static final Pattern CEP_PATTERN = Pattern.compile("\\b\\d{5}-?\\d{3}\\b");
	private static final Pattern EMAIL_PATTERN = Pattern
			.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern TELEFONE_PATTERN = Pattern
			.compile("(?:\\(?\\d{2}\\)?\\s?)?9?\\d{4}[-\\s]?\\d{4}");
	private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final String[] ROTULOS_FIM_IDENTIFICACAO = { "o presente question", "questionario de anam",
			"anamnese", "historia clinica", "historia medica", "historico de saude", "termo de consentimento",
			"odontograma", "procedimentos", "plano de tratamento", "queixa principal" };
	private static final String[] ROTULOS_DADOS_PESSOAIS = { "identifica", "dados pessoais", "dados do paciente",
			"cadastro do paciente" };
	private static final double IDENTIFICACAO_Y_INICIAL = 0.10;
	private static final double IDENTIFICACAO_ALTURA = 0.30;

	private final PacienteRepository pacienteRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${app.ocr.command:}")
	private String ocrCommand;

	@Value("${app.ocr.language:por}")
	private String ocrLanguage;

	@Value("${app.ocr.tessdata-dir:}")
	private String tessdataDir;

	@Value("${app.ocr.openai.enabled:false}")
	private boolean openAiOcrEnabled;

	@Value("${app.ocr.openai.api-key:}")
	private String openAiApiKey;

	@Value("${app.ocr.openai.model:gpt-4.1}")
	private String openAiModel;

	public FichaPacienteImportacaoService(PacienteRepository pacienteRepository) {
		this.pacienteRepository = pacienteRepository;
	}

	public FichaPacientePreviewDTO extrair(MultipartFile arquivo) {
		FichaPacientePreviewDTO preview = new FichaPacientePreviewDTO();
		PacienteFichaDTO paciente = new PacienteFichaDTO();
		preview.setPaciente(paciente);

		if (openAiOcrEnabled && chaveOpenAi() == null) {
			preview.setAviso("Chave OpenAI nao configurada. Campos manuscritos precisam de OPENAI_API_KEY ou app.ocr.openai.api-key.");
		}

		if (openAiOcrConfigurado()) {
			try {
				PacienteFichaDTO pacienteComManuscrito = extrairPacienteComVisao(arquivo);
				preview.setPaciente(pacienteComManuscrito);
				preview.setTextoExtraido(formatarIdentificacao(pacienteComManuscrito));
				return preview;
			} catch (Exception e) {
				preview.setAviso("Nao foi possivel ler manuscrito com IA: " + e.getMessage()
						+ ". Tentando OCR local.");
			}
		}

		if (ocrCommand == null || ocrCommand.isBlank()) {
			preview.setAviso("OCR nao configurado. Para ler campos preenchidos a caneta, configure app.ocr.openai.");
			return preview;
		}

		try {
			String texto = executarOcr(arquivo);
			String identificacao = secaoIdentificacao(texto);
			preview.setTextoExtraido(identificacao);
			preview.setPaciente(extrairPaciente(identificacao));
			if (texto == null || texto.isBlank()) {
				preview.setAviso("Nenhum texto foi extraido do arquivo. Preencha os dados manualmente.");
			} else if (identificacao == null || identificacao.isBlank()) {
				preview.setAviso("Nao foi encontrada a secao de identificacao. Revise os dados antes de cadastrar.");
			} else if (preview.getAviso() == null || preview.getAviso().isBlank()) {
				preview.setAviso("OCR local pode nao reconhecer campos preenchidos a caneta. Para manuscrito, configure app.ocr.openai.");
			}
		} catch (Exception e) {
			preview.setAviso("Nao foi possivel executar OCR: " + e.getMessage());
		}

		return preview;
	}

	private boolean openAiOcrConfigurado() {
		return openAiOcrEnabled && chaveOpenAi() != null;
	}

	private String chaveOpenAi() {
		if (openAiApiKey != null && !openAiApiKey.isBlank()) {
			return openAiApiKey;
		}
		String ambiente = System.getenv("OPENAI_API_KEY");
		if (ambiente != null && !ambiente.isBlank()) {
			return ambiente;
		}
		return null;
	}

	private PacienteFichaDTO extrairPacienteComVisao(MultipartFile arquivo) throws IOException, InterruptedException {
		ObjectNode body = objectMapper.createObjectNode();
		body.put("model", openAiModel);
		body.put("temperature", 0);
		body.put("max_output_tokens", 700);

		ArrayNode input = body.putArray("input");
		ObjectNode message = input.addObject();
		message.put("role", "user");
		ArrayNode content = message.putArray("content");
		content.addObject().put("type", "input_text").put("text", promptExtracaoIdentificacao());
		for (ImagemEntrada imagem : imagensParaVisao(arquivo)) {
			content.addObject().put("type", "input_image").put("image_url", imagem.dataUrl()).put("detail",
					imagem.detail());
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.openai.com/v1/responses"))
				.header("Authorization", "Bearer " + chaveOpenAi())
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException("OpenAI retornou HTTP " + response.statusCode());
		}

		String output = extrairTextoRespostaOpenAi(response.body());
		return objectMapper.readValue(extrairJson(output), PacienteFichaDTO.class);
	}

	private String promptExtracaoIdentificacao() {
		return """
				Leia os campos manuscritos ou digitados da secao IDENTIFICACAO DO PACIENTE.
				A primeira imagem e um recorte ampliado da identificacao; use-a como fonte principal.
				A segunda imagem, se existir, e a pagina completa apenas para contexto.
				Ignore completamente anamnese, perguntas clinicas e marcacoes de sim/nao.
				Retorne apenas JSON valido, sem markdown, com estas chaves:
				nome, cpf, telefone, whatsapp, email, dataNascimento, endereco, numero, complemento, bairro, cep, cidade, uf.
				Transcreva exatamente o que conseguir ler, inclusive quando a letra estiver cursiva.
				Use dataNascimento no formato yyyy-MM-dd quando for possivel.
				Use null para campos ausentes ou ilegíveis.
				""";
	}

	private ImagemEntrada[] imagensParaVisao(MultipartFile arquivo) throws IOException {
		String original = arquivo.getOriginalFilename() == null ? "ficha" : arquivo.getOriginalFilename();
		String extensao = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
		BufferedImage pagina;
		if (isPdf(original, extensao)) {
			Path temporario = Files.createTempFile("ficha-paciente-openai-", ".pdf");
			try {
				Files.write(temporario, arquivo.getBytes());
				pagina = renderizarPrimeiraPaginaPdfComoImagem(temporario);
			} finally {
				Files.deleteIfExists(temporario);
			}
		} else {
			pagina = ImageIO.read(arquivo.getInputStream());
		}

		if (pagina == null) {
			throw new IllegalStateException("arquivo de imagem invalido");
		}

		BufferedImage identificacao = recortarIdentificacao(pagina);
		return new ImagemEntrada[] {
				new ImagemEntrada("data:image/png;base64," + Base64.getEncoder().encodeToString(pngBytes(identificacao)),
						"high"),
				new ImagemEntrada("data:image/png;base64," + Base64.getEncoder().encodeToString(pngBytes(pagina)),
						"low") };
	}

	private BufferedImage renderizarPrimeiraPaginaPdfComoImagem(Path pdf) throws IOException {
		try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
			if (document.getNumberOfPages() == 0) {
				throw new IllegalStateException("PDF sem paginas para leitura.");
			}
			PDFRenderer renderer = new PDFRenderer(document);
			return renderer.renderImageWithDPI(0, 300, ImageType.RGB);
		}
	}

	private BufferedImage recortarIdentificacao(BufferedImage pagina) {
		int y = Math.max(0, (int) (pagina.getHeight() * IDENTIFICACAO_Y_INICIAL));
		int altura = Math.min(pagina.getHeight() - y, (int) (pagina.getHeight() * IDENTIFICACAO_ALTURA));
		BufferedImage recorte = pagina.getSubimage(0, y, pagina.getWidth(), altura);
		int escala = 2;
		BufferedImage ampliado = new BufferedImage(recorte.getWidth() * escala, recorte.getHeight() * escala,
				BufferedImage.TYPE_INT_RGB);
		var graphics = ampliado.createGraphics();
		graphics.drawImage(recorte, 0, 0, ampliado.getWidth(), ampliado.getHeight(), null);
		graphics.dispose();
		return ampliado;
	}

	private byte[] pngBytes(BufferedImage imagem) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(imagem, "png", output);
		return output.toByteArray();
	}

	private record ImagemEntrada(String dataUrl, String detail) {
	}

	private String extrairTextoRespostaOpenAi(String json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		JsonNode outputText = root.path("output_text");
		if (outputText.isTextual()) {
			return outputText.asText();
		}
		for (JsonNode output : root.path("output")) {
			for (JsonNode content : output.path("content")) {
				JsonNode text = content.path("text");
				if (text.isTextual()) {
					return text.asText();
				}
			}
		}
		throw new IllegalStateException("resposta sem texto");
	}

	private String extrairJson(String texto) {
		int inicio = texto.indexOf('{');
		int fim = texto.lastIndexOf('}');
		if (inicio < 0 || fim <= inicio) {
			throw new IllegalStateException("resposta nao contem JSON");
		}
		return texto.substring(inicio, fim + 1);
	}

	private String formatarIdentificacao(PacienteFichaDTO paciente) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(paciente);
		} catch (IOException e) {
			return "";
		}
	}

	public PacienteEntity salvar(Long tenantId, PacienteFichaDTO dto) {
		PacienteEntity paciente = buscarExistente(tenantId, dto);
		paciente.setTenantId(tenantId);
		paciente.setNome(dto.getNome());
		paciente.setCpf(dto.getCpf());
		paciente.setTelefone(dto.getTelefone());
		paciente.setWhatsapp(dto.getWhatsapp());
		paciente.setEmail(dto.getEmail());
		paciente.setEndereco(dto.getEndereco());
		paciente.setNumero(dto.getNumero());
		paciente.setComplemento(dto.getComplemento());
		paciente.setBairro(dto.getBairro());
		paciente.setCep(dto.getCep());
		paciente.setCidade(dto.getCidade());
		paciente.setUf(dto.getUf());
		paciente.setAtivo(true);

		if (dto.getDataNascimento() != null && !dto.getDataNascimento().isBlank()) {
			paciente.setDataNascimento(LocalDate.parse(dto.getDataNascimento()));
		}

		return pacienteRepository.save(paciente);
	}

	private PacienteEntity buscarExistente(Long tenantId, PacienteFichaDTO dto) {
		if (dto.getCpf() == null || dto.getCpf().isBlank()) {
			return new PacienteEntity();
		}
		return pacienteRepository.findByTenantIdAndCpf(tenantId, dto.getCpf()).orElseGet(PacienteEntity::new);
	}

	private String executarOcr(MultipartFile arquivo) throws IOException, InterruptedException {
		String original = arquivo.getOriginalFilename() == null ? "ficha" : arquivo.getOriginalFilename();
		String extensao = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".img";
		Path temporario = Files.createTempFile("ficha-paciente-", extensao);
		arquivo.transferTo(temporario);
		Path arquivoOcr = temporario;

		try {
			if (isPdf(original, extensao)) {
				arquivoOcr = renderizarPrimeiraPaginaPdf(temporario);
			}

			ProcessBuilder builder = criarProcessoOcr(arquivoOcr);
			Process process = builder.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
			int exit = process.waitFor();
			if (exit != 0) {
				throw new IllegalStateException(error.isBlank() ? "comando OCR retornou codigo " + exit : error);
			}
			return output;
		} finally {
			if (!arquivoOcr.equals(temporario)) {
				Files.deleteIfExists(arquivoOcr);
			}
			Files.deleteIfExists(temporario);
		}
	}

	private boolean isPdf(String original, String extensao) {
		return ".pdf".equalsIgnoreCase(extensao) || original.toLowerCase(Locale.ROOT).endsWith(".pdf");
	}

	private Path renderizarPrimeiraPaginaPdf(Path pdf) throws IOException {
		Path imagem = Files.createTempFile("ficha-paciente-pdf-", ".png");
		try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
			if (document.getNumberOfPages() == 0) {
				throw new IllegalStateException("PDF sem paginas para leitura.");
			}
			PDFRenderer renderer = new PDFRenderer(document);
			BufferedImage pagina = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
			ImageIO.write(pagina, "png", imagem.toFile());
			return imagem;
		}
	}

	private ProcessBuilder criarProcessoOcr(Path temporario) {
		if (tessdataDir == null || tessdataDir.isBlank()) {
			return new ProcessBuilder(ocrCommand, temporario.toString(), "stdout", "-l", ocrLanguage);
		}
		return new ProcessBuilder(ocrCommand, temporario.toString(), "stdout", "-l", ocrLanguage, "--tessdata-dir",
				tessdataDir);
	}

	private PacienteFichaDTO extrairPaciente(String texto) {
		PacienteFichaDTO paciente = new PacienteFichaDTO();
		if (texto == null) {
			return paciente;
		}

		String identificacao = texto.replace("\r", "\n");
		paciente.setNome(limparNome(primeiroCampo(identificacao, "nome completo", "nome do paciente", "paciente",
				"nome")));
		paciente.setTelefone(primeiroCampo(identificacao, "telefone", "tel", "fone"));
		paciente.setWhatsapp(primeiroCampo(identificacao, "whatsapp", "celular", "cel"));
		if (paciente.getWhatsapp() == null || paciente.getWhatsapp().isBlank()) {
			paciente.setWhatsapp(primeiroMatch(TELEFONE_PATTERN, identificacao));
		}
		paciente.setEmail(primeiroCampo(identificacao, "e-mail", "email"));
		if (paciente.getEmail() == null || paciente.getEmail().isBlank()) {
			paciente.setEmail(primeiroMatch(EMAIL_PATTERN, identificacao));
		}
		paciente.setEndereco(primeiroCampo(identificacao, "endereco", "logradouro", "rua", "avenida"));
		paciente.setNumero(primeiroCampo(identificacao, "numero", "no", "n"));
		paciente.setComplemento(primeiroCampo(identificacao, "complemento"));
		paciente.setBairro(primeiroCampo(identificacao, "bairro"));
		paciente.setCidade(primeiroCampo(identificacao, "cidade", "municipio"));
		paciente.setUf(normalizarUf(primeiroCampo(identificacao, "uf", "estado")));
		paciente.setCpf(primeiroMatch(CPF_PATTERN, identificacao));
		paciente.setCep(primeiroMatch(CEP_PATTERN, identificacao));

		String nascimento = primeiroCampo(identificacao, "data de nascimento", "nascimento", "data nasc", "dt nasc");
		paciente.setDataNascimento(normalizarData(nascimento));

		return paciente;
	}

	private String secaoIdentificacao(String texto) {
		if (texto == null || texto.isBlank()) {
			return texto;
		}
		texto = texto.replace("\r", "\n");
		String minusculo = texto.toLowerCase(Locale.ROOT);
		int inicio = indiceInicioIdentificacao(minusculo);
		if (inicio < 0) {
			return textoParaCamposDeIdentificacao(texto);
		}

		int fim = indiceFinalSecao(minusculo, inicio);
		if (fim > inicio) {
			return texto.substring(inicio, fim);
		}
		return texto.substring(inicio);
	}

	private int indiceFinalSecao(String minusculo, int inicio) {
		String[] marcadores = { "o presente question", "questionario de anam", "historia clinica",
				"história clínica", "termo de consentimento" };
		int fim = -1;
		for (String marcador : ROTULOS_FIM_IDENTIFICACAO) {
			int indice = minusculo.indexOf(marcador, inicio + 1);
			if (indice > inicio && (fim < 0 || indice < fim)) {
				fim = indice;
			}
		}
		return fim;
	}

	private int indiceInicioIdentificacao(String minusculo) {
		int inicio = -1;
		for (String rotulo : ROTULOS_DADOS_PESSOAIS) {
			int indice = minusculo.indexOf(rotulo);
			if (indice >= 0 && (inicio < 0 || indice < inicio)) {
				inicio = indice;
			}
		}
		return inicio;
	}

	private String textoParaCamposDeIdentificacao(String texto) {
		StringBuilder campos = new StringBuilder();
		for (String linha : texto.split("\\n")) {
			if (linhaEhIdentificacao(linha)) {
				campos.append(linha.strip()).append(System.lineSeparator());
			}
		}
		return campos.isEmpty() ? texto : campos.toString().trim();
	}

	private boolean linhaEhIdentificacao(String linha) {
		String minusculo = linha.toLowerCase(Locale.ROOT);
		String[] rotulos = { "nome", "paciente", "cpf", "rg", "data de nascimento", "nascimento", "telefone", "tel",
				"celular", "whatsapp", "email", "e-mail", "endereco", "logradouro", "rua", "avenida", "numero",
				"complemento", "bairro", "cep", "cidade", "municipio", "uf", "estado" };
		for (String rotulo : rotulos) {
			if (minusculo.contains(rotulo)) {
				return true;
			}
		}
		return CPF_PATTERN.matcher(linha).find() || CEP_PATTERN.matcher(linha).find()
				|| EMAIL_PATTERN.matcher(linha).find();
	}

	private String primeiroCampo(String texto, String... rotulos) {
		for (String rotulo : rotulos) {
			String valor = campoAposRotuloNoInicioDaLinha(texto, rotulo);
			if (valor != null && !valor.isBlank()) {
				return valor;
			}
		}
		return null;
	}

	private String campoAposRotuloNoInicioDaLinha(String texto, String rotulo) {
		Pattern pattern = Pattern.compile("(?im)^\\s*" + Pattern.quote(rotulo)
				+ "\\s*(?:[:;\\-]|\\.|\\s{2,})\\s*([^\\n]+)");
		Matcher matcher = pattern.matcher(texto);
		if (!matcher.find()) {
			return null;
		}
		String valor = matcher.group(1).trim();
		return limparValorAteProximoRotulo(valor);
	}

	private String limparValorAteProximoRotulo(String valor) {
		String minusculo = valor.toLowerCase(Locale.ROOT);
		String[] rotulos = { " telefone", " celular", " whatsapp", " cpf", " rg", " cep", " endereco", " endereço",
				" data de nascimento" };
		for (String rotulo : rotulos) {
			int indice = minusculo.indexOf(rotulo);
			if (indice > 0) {
				valor = valor.substring(0, indice).trim();
				break;
			}
		}
		return valor.isBlank() ? null : valor;
	}

	private String primeiroMatch(Pattern pattern, String texto) {
		Matcher matcher = pattern.matcher(texto);
		return matcher.find() ? matcher.group().trim() : null;
	}

	private String normalizarUf(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		Matcher matcher = Pattern.compile("\\b[A-Z]{2}\\b").matcher(valor.toUpperCase(Locale.ROOT));
		return matcher.find() ? matcher.group() : null;
	}

	private String normalizarData(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		Matcher matcher = Pattern.compile("\\d{2}/\\d{2}/\\d{4}").matcher(valor);
		if (!matcher.find()) {
			return null;
		}
		return LocalDate.parse(matcher.group(), INPUT_DATE).toString();
	}

	private String limparNome(String valor) {
		if (valor == null) {
			return null;
		}
		String minusculo = valor.toLowerCase(Locale.ROOT);
		String[] termosInvalidos = { "telefone", "celular", "whatsapp", "tel.", " tel", "médico", "medico",
				"emerg", "cpf", "rg", "cep", "estado civil" };
		for (String termo : termosInvalidos) {
			if (minusculo.contains(termo)) {
				return null;
			}
		}
		if (valor.length() < 5) {
			return null;
		}
		return valor;
	}
}
