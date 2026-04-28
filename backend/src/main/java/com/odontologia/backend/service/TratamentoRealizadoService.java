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
import java.time.LocalDateTime;
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
		TratamentoRealizadoEntity tratamento = new TratamentoRealizadoEntity();
		tratamento.setTenantId(tenantId);
		preencherTratamento(tenantId, tratamento, dto);

		return repository.save(tratamento);
	}

	@Transactional
	public TratamentoRealizadoEntity atualizar(Long tenantId, Long id, TratamentoRealizadoRequestDTO dto) {
		TratamentoRealizadoEntity tratamento = repository.findById(id).orElseThrow();
		if (!tratamento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Tratamento nao encontrado.");
		}

		preencherTratamento(tenantId, tratamento, dto);
		return repository.save(tratamento);
	}

	@Transactional
	public void excluir(Long tenantId, Long id) {
		TratamentoRealizadoEntity tratamento = repository.findById(id).orElseThrow();
		if (!tratamento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Tratamento nao encontrado.");
		}

		repository.delete(tratamento);
	}

	@Transactional
	public TratamentoRealizadoEntity finalizar(Long tenantId, Long id) {
		TratamentoRealizadoEntity tratamento = repository.findById(id).orElseThrow();
		if (!tratamento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Tratamento nao encontrado.");
		}

		tratamento.setFinalizado(true);
		tratamento.setDataFinalizacao(LocalDateTime.now());
		return repository.save(tratamento);
	}

	private void preencherTratamento(Long tenantId, TratamentoRealizadoEntity tratamento, TratamentoRealizadoRequestDTO dto) {
		if (dto.getPacienteId() == null) {
			throw new RuntimeException("Paciente e obrigatorio.");
		}
		if (dto.getValorPago() == null || dto.getValorPago().compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Valor pago deve ser informado.");
		}
		BigDecimal valorTratamento = dto.getValorTratamento() == null ? BigDecimal.ZERO : dto.getValorTratamento();
		if (valorTratamento.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Valor do tratamento deve ser informado.");
		}
		BigDecimal valorTotal = dto.getValorTotal() == null ? valorTratamento : dto.getValorTotal();
		if (valorTotal.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Valor total deve ser informado.");
		}
		BigDecimal totalPagoAnterior = repository.totalPagoPacienteExcluindoTratamento(tenantId, dto.getPacienteId(),
				tratamento.getId());
		BigDecimal totalPagoAtualizado = totalPagoAnterior.add(dto.getValorPago());
		if (totalPagoAtualizado.compareTo(valorTotal) > 0) {
			throw new RuntimeException("A soma dos pagamentos nao pode ser maior que o valor total da avaliacao.");
		}

		tratamento.setPacienteId(dto.getPacienteId());
		tratamento.setProcedimentoId(dto.getProcedimentoId());
		tratamento.setTratamento(resolveTratamento(dto));
		tratamento.setDente(normalizarDente(dto.getDente()));
		tratamento.setValorPago(dto.getValorPago());
		tratamento.setValorTratamento(valorTratamento);
		tratamento.setValorTotal(valorTotal);
		tratamento.setSaldo(valorTotal.subtract(totalPagoAtualizado));
		tratamento.setFormaPagamento(normalizarTexto(dto.getFormaPagamento()));
		tratamento.setParcelas(normalizarParcelas(dto));
		tratamento.setDataRealizacao(dto.getDataRealizacao() == null ? LocalDate.now() : dto.getDataRealizacao());
		if (tratamento.getFinalizado() == null) {
			tratamento.setFinalizado(false);
		}
		tratamento.setObservacoes(dto.getObservacoes());
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

	private String normalizarDente(String dente) {
		return normalizarTexto(dente);
	}

	private String normalizarTexto(String valor) {
		if (valor == null || valor.trim().isEmpty()) {
			return null;
		}
		return valor.trim();
	}

	private Integer normalizarParcelas(TratamentoRealizadoRequestDTO dto) {
		if (!"CARTAO_CREDITO_PARCELADO".equals(dto.getFormaPagamento())) {
			return null;
		}
		if (dto.getParcelas() == null || dto.getParcelas() < 2) {
			throw new RuntimeException("Informe a quantidade de parcelas do cartao de credito.");
		}
		return dto.getParcelas();
	}
}
