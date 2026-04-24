package com.odontologia.backend.service.auth;

import com.odontologia.backend.dto.auth.LoginRequestDTO;
import com.odontologia.backend.dto.auth.LoginResponseDTO;
import com.odontologia.backend.service.UsuarioAuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioAuthService authService;

	public AuthController(UsuarioAuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
		return authService.login(request);
	}
}