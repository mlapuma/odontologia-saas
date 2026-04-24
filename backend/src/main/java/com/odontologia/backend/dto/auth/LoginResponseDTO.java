package com.odontologia.backend.dto.auth;

public class LoginResponseDTO {

	private String token;
	private String nome;
	private String email;
	private Long tenantId;
	private String perfil;

	public LoginResponseDTO() {
	}

	public LoginResponseDTO(String token, String nome, String email, Long tenantId, String perfil) {
		this.token = token;
		this.nome = nome;
		this.email = email;
		this.tenantId = tenantId;
		this.perfil = perfil;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}
}