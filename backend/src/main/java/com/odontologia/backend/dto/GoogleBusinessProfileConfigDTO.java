package com.odontologia.backend.dto;

import java.util.List;

public class GoogleBusinessProfileConfigDTO {

	private boolean configurado;
	private boolean autorizado;
	private boolean clientIdConfigurado;
	private boolean clientSecretConfigurado;
	private String redirectUri;
	private String authorizationUrl;
	private String mensagem;
	private List<String> escopos;

	public boolean isConfigurado() {
		return configurado;
	}

	public void setConfigurado(boolean configurado) {
		this.configurado = configurado;
	}

	public boolean isAutorizado() {
		return autorizado;
	}

	public void setAutorizado(boolean autorizado) {
		this.autorizado = autorizado;
	}

	public boolean isClientIdConfigurado() {
		return clientIdConfigurado;
	}

	public void setClientIdConfigurado(boolean clientIdConfigurado) {
		this.clientIdConfigurado = clientIdConfigurado;
	}

	public boolean isClientSecretConfigurado() {
		return clientSecretConfigurado;
	}

	public void setClientSecretConfigurado(boolean clientSecretConfigurado) {
		this.clientSecretConfigurado = clientSecretConfigurado;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public List<String> getEscopos() {
		return escopos;
	}

	public void setEscopos(List<String> escopos) {
		this.escopos = escopos;
	}
}
