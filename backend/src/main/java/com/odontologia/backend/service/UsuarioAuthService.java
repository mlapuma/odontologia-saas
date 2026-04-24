package com.odontologia.backend.service;

import com.odontologia.backend.dto.auth.LoginRequestDTO;
import com.odontologia.backend.dto.auth.LoginResponseDTO;
import com.odontologia.backend.entity.UsuarioEntity;
import com.odontologia.backend.repository.UsuarioRepository;
import com.odontologia.backend.service.auth.JwtService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UsuarioAuthService {

	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;

	public UsuarioAuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.jwtService = jwtService;
	}

	public LoginResponseDTO login(LoginRequestDTO request) {
		UsuarioEntity usuario = usuarioRepository.findByEmailAndAtivoTrue(request.getEmail())
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		// Temporário: comparação simples
		// Depois podemos trocar por BCrypt
		if (!usuario.getSenhaHash().equals(request.getSenha())) {
			throw new RuntimeException("Senha inválida");
		}

		usuario.setUltimoLogin(LocalDateTime.now());
		usuarioRepository.save(usuario);

		String token = jwtService.generateToken(usuario.getId(), usuario.getTenantId(), usuario.getEmail(),
				usuario.getPerfil());

		return new LoginResponseDTO(token, usuario.getNome(), usuario.getEmail(), usuario.getTenantId(),
				usuario.getPerfil());
	}
}