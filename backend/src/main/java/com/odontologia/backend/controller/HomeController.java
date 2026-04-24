package com.odontologia.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Odonto SaaS API está funcionando!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
