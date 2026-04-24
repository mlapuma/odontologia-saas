package com.odontologia.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class OdontologiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OdontologiaApplication.class, args);
	}
}