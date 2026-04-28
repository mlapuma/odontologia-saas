package com.odontologia.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.odontologia.backend.entity.PacienteEntity;
import com.odontologia.backend.repository.PacienteRepository;

@Service
public class PacienteService {

	private final PacienteRepository repository;

	public PacienteService(PacienteRepository repository) {
		this.repository = repository;
	}

	public List<PacienteEntity> listar(Long tenantId) {
		return repository.findByTenantId(tenantId);
	}

	public PacienteEntity buscar(Long id) {
		return repository.findById(id).orElseThrow();
	}

	public PacienteEntity salvar(PacienteEntity paciente) {
		if (paciente.getTenantId() == null) {
			throw new RuntimeException("Id da Clínica é obrigatório");
		}
		return repository.save(paciente);
	}

	public PacienteEntity atualizar(Long id, PacienteEntity paciente) {

		PacienteEntity existente = buscar(id);

		existente.setNome(paciente.getNome());
		existente.setCpf(paciente.getCpf());
		existente.setTelefone(paciente.getTelefone());
		existente.setWhatsapp(paciente.getWhatsapp());
		existente.setEmail(paciente.getEmail());
		existente.setEndereco(paciente.getEndereco());
		existente.setNumero(paciente.getNumero());
		existente.setComplemento(paciente.getComplemento());
		existente.setBairro(paciente.getBairro());
		existente.setCep(paciente.getCep());
		existente.setDataNascimento(paciente.getDataNascimento());
		existente.setCidade(paciente.getCidade());
		existente.setUf(paciente.getUf());

		return repository.save(existente);
	}

	public void deletar(Long id) {
		repository.deleteById(id);
	}
}
