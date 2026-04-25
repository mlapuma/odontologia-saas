package com.odontologia.backend.service;

import com.odontologia.backend.dto.PacienteReativacaoDTO;
import com.odontologia.backend.dto.TratamentoRealizadoRequestDTO;
import com.odontologia.backend.entity.ProcedimentoEntity;
import com.odontologia.backend.entity.TratamentoRealizadoEntity;
import com.odontologia.backend.repository.ProcedimentoRepository;
import com.odontologia.backend.repository.TratamentoRealizadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TratamentoRealizadoService {

	private final TratamentoRealizadoRepository repository;
	private final ProcedimentoRepository procedimentoRepository;

	public TratamentoRealizadoService(TratamentoRealizadoRepository repository,
			ProcedimentoRepository procedimentoRepository) {
		this.repository = repository;
		this.procedimentoRepository = procedimentoRepository;
	}

	public List<TratamentoRealizadoEntity> listar(Long tenantId, Long pacienteId) {
		if (pacienteId != null) {
			return repository.findByTenantIdAndPacienteIdOrderByDataRealizacaoDescCreatedAtDesc(tenantId, pacienteId);
		}
		return repository.findByTenantIdOrderByDataRealizacaoDescCreatedAtDesc(tenantId);
	}

	@Transactional
	public TratamentoRealizadoEntity criar(Long tenantId, TratamentoRealizadoRequestDTO dto) {
		if (dto.getPacienteId() == null) {
			throw new RuntimeException("Paciente e obrigatorio.");
		}
		if (dto.getValorPago() == null || dto.getValorPago().compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Valor pago deve ser informado.");
		}

		TratamentoRealizadoEntity tratamento = new TratamentoRealizadoEntity();
		tratamento.setTenantId(tenantId);
		tratamento.setPacienteId(dto.getPacienteId());
		tratamento.setProcedimentoId(dto.getProcedimentoId());
		tratamento.setTratamento(resolveTratamento(dto));
		tratamento.setValorPago(dto.getValorPago());
		tratamento.setDataRealizacao(dto.getDataRealizacao() == null ? LocalDate.now() : dto.getDataRealizacao());
		tratamento.setObservacoes(dto.getObservacoes());

		return repository.save(tratamento);
	}

	public List<PacienteReativacaoDTO> pacientesParaReativacao(Long tenantId, int diasSemComparecer) {
		LocalDate limite = LocalDate.now().minusDays(diasSemComparecer);
		return repository.buscarPacientesParaReativacao(tenantId, limite);
	}

	private String resolveTratamento(TratamentoRealizadoRequestDTO dto) {
		if (dto.getTratamento() != null && !dto.getTratamento().trim().isEmpty()) {
			return dto.getTratamento().trim();
		}
		if (dto.getProcedimentoId() != null) {
			return procedimentoRepository.findById(dto.getProcedimentoId())
					.map(ProcedimentoEntity::getNome)
					.orElseThrow(() -> new RuntimeException("Procedimento nao encontrado."));
		}
		throw new RuntimeException("Informe o tratamento ou selecione um procedimento.");
	}
}
