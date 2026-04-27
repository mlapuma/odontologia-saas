package com.odontologia.backend.controller;

import com.odontologia.backend.entity.ProfissionalEntity;
import com.odontologia.backend.repository.ProfissionalRepository;
import com.odontologia.backend.security.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

	private final ProfissionalRepository repository;

	public ProfissionalController(ProfissionalRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<ProfissionalEntity> listar() {
		return repository.findByTenantIdOrderByNomeAsc(TenantContext.getTenantId());
	}

	@PostMapping
	public ProfissionalEntity criar(@RequestBody ProfissionalEntity profissional) {
		profissional.setTenantId(TenantContext.getTenantId());
		return repository.save(profissional);
	}

	@PutMapping("/{id}")
	public ProfissionalEntity atualizar(@PathVariable Long id, @RequestBody ProfissionalEntity profissional) {
		ProfissionalEntity atual = repository.findById(id).orElseThrow();
		atual.setNome(profissional.getNome());
		atual.setEspecialidade(profissional.getEspecialidade());
		atual.setCro(profissional.getCro());
		atual.setTelefone(profissional.getTelefone());
		atual.setEmail(profissional.getEmail());
		atual.setAtivo(profissional.getAtivo());
		return repository.save(atual);
	}

	@DeleteMapping("/{id}")
	public void excluir(@PathVariable Long id) {
		repository.deleteById(id);
	}
}
