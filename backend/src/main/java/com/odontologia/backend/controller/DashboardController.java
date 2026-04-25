package com.odontologia.backend.controller;

import com.odontologia.backend.dto.DashboardResumoDTO;
import com.odontologia.backend.security.TenantContext;
import com.odontologia.backend.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardService service;

	public DashboardController(DashboardService service) {
		this.service = service;
	}

	@GetMapping
	public DashboardResumoDTO resumo(@RequestParam(required = false) Long tenantId) {
		Long tenant = TenantContext.getTenantId() == null ? tenantId : TenantContext.getTenantId();
		return service.obterResumo(tenant);
	}
}
