package com.odontologia.backend.controller;

import com.odontologia.backend.dto.GoogleBusinessProfileConfigDTO;
import com.odontologia.backend.service.GoogleBusinessProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integracoes/google-business-profile")
public class IntegracaoGoogleBusinessProfileController {

	private final GoogleBusinessProfileService service;

	public IntegracaoGoogleBusinessProfileController(GoogleBusinessProfileService service) {
		this.service = service;
	}

	@GetMapping
	public GoogleBusinessProfileConfigDTO status() {
		return service.obterStatus();
	}

	@GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> callback(@RequestParam(required = false) String code,
			@RequestParam(required = false) String error) {
		if (error != null && !error.isBlank()) {
			return ResponseEntity.badRequest().body(service.paginaCallbackErro(error));
		}

		try {
			service.processarCallback(code);
			return ResponseEntity.ok(service.paginaCallbackSucesso());
		} catch (RuntimeException ex) {
			return ResponseEntity.badRequest().body(service.paginaCallbackErro(ex.getMessage()));
		}
	}
}
