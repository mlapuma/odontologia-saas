package com.odontologia.backend.controller;

import com.odontologia.backend.dto.FichaPacientePreviewDTO;
import com.odontologia.backend.dto.PacienteFichaDTO;
import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.FichaPacienteImportacaoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pacientes/importacao-ficha")
public class FichaPacienteImportacaoController {

	private final FichaPacienteImportacaoService service;

	public FichaPacienteImportacaoController(FichaPacienteImportacaoService service) {
		this.service = service;
	}

	@PostMapping("/preview")
	public FichaPacientePreviewDTO preview(@RequestParam("arquivo") MultipartFile arquivo) {
		return service.extrair(arquivo);
	}

	@PostMapping("/salvar")
	public PacienteEntity salvar(@RequestBody PacienteFichaDTO dto) {
		return service.salvar(TenantContext.getTenantId(), dto);
	}
}
