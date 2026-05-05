package com.odontologia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

	@Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
	private String allowedOrigins;

	@Bean
	CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(origins());
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsFilter(source);
	}

	private List<String> origins() {
		return Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(origin -> !origin.isBlank())
				.toList();
	}
}
