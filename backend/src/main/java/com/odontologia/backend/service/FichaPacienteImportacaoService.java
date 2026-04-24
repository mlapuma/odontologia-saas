package com.odontologia.backend.service;

import com.odontologia.backend.dto.FichaPacientePreviewDTO;
import com.odontologia.backend.dto.PacienteFichaDTO;
import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.repository.PacienteRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FichaPacienteImportacaoService {

	private static final Pattern CPF_PATTERN = Pattern.compile("\\b\\d{3}[.]?\\d{3}[.]?\\d{3}-?\\d{2}\\b");
	private static final Pattern CEP_PATTERN = Pattern.compile("\\b\\d{5}-?\\d{3}\\b");
	private static final Pattern TELEFONE_PATTERN = Pattern
			.compile("(?:\\(?\\d{2}\\)?\\s?)?9?\\d{4}[-\\s]?\\d{4}");
	private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final PacienteRepository pacienteRepository;

	@Value("${app.ocr.command:}")
	private String ocrCommand;

	@Value("${app.ocr.language:por}")
	private String ocrLanguage;

	@Value("${app.ocr.tessdata-dir:}")
	private String tessdataDir;

	public FichaPacienteImportacaoService(PacienteRepository pacienteRepository) {
		this.pacienteRepository = pacienteRepository;
	}

	public FichaPacientePreviewDTO extrair(MultipartFile arquivo) {
		FichaPacientePreviewDTO preview = new FichaPacientePreviewDTO();
		PacienteFichaDTO paciente = new PacienteFichaDTO();
		preview.setPaciente(paciente);

		if (ocrCommand == null || ocrCommand.isBlank()) {
			preview.setAviso("OCR nao configurado. Revise e preencha os dados antes de cadastrar.");
			return preview;
		}

		try {
			String texto = executarOcr(arquivo);
			preview.setTextoExtraido(texto);
			preview.setPaciente(extrairPaciente(texto));
			if (texto == null || texto.isBlank()) {
				preview.setAviso("Nenhum texto foi extraido do arquivo. Preencha os dados manualmente.");
			}
		} catch (Exception e) {
			preview.setAviso("Nao foi possivel executar OCR: " + e.getMessage());
		}

		return preview;
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

		String normalizado = texto.replace("\r", "\n");
		String identificacao = secaoIdentificacao(normalizado);
		paciente.setNome(limparNome(campoAposRotuloNoInicioDaLinha(identificacao, "nome")));
		paciente.setEndereco(campoAposRotuloNoInicioDaLinha(identificacao, "endereco"));
		paciente.setCpf(primeiroMatch(CPF_PATTERN, identificacao));
		paciente.setCep(primeiroMatch(CEP_PATTERN, identificacao));
		paciente.setWhatsapp(primeiroMatch(TELEFONE_PATTERN, identificacao));

		String nascimento = campoAposRotuloNoInicioDaLinha(identificacao, "data de nascimento");
		paciente.setDataNascimento(normalizarData(nascimento));

		return paciente;
	}

	private String secaoIdentificacao(String texto) {
		String minusculo = texto.toLowerCase(Locale.ROOT);
		int inicio = minusculo.indexOf("identifica");
		if (inicio < 0) {
			return texto;
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
		for (String marcador : marcadores) {
			int indice = minusculo.indexOf(marcador, inicio + 1);
			if (indice > inicio && (fim < 0 || indice < fim)) {
				fim = indice;
			}
		}
		return fim;
	}

	private String campoAposRotuloNoInicioDaLinha(String texto, String rotulo) {
		Pattern pattern = Pattern.compile("(?im)^\\s*" + Pattern.quote(rotulo) + "\\s*:?\\s*([^\\n]+)");
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
