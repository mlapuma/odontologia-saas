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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TratamentoRealizadoService {

	private final TratamentoRealizadoRepository repository;
	private final ProcedimentoRepository procedimentoRepository;

	public TratamentoRealizadoService(TratamentoRealizadoRepository repository,
			ProcedimentoRepository procedimentoRepository) {
		this.repository = repository;
		this.procedimentoRepository = procedimentoRepository;
	}

	@Transactional
	public List<TratamentoRealizadoEntity> listar(Long tenantId, Long pacienteId) {
		if (pacienteId != null) {
			sincronizarSaldoAvaliacaoAtual(tenantId, pacienteId);
			return repository.findByTenantIdAndPacienteIdOrderByDataRealizacaoDescCreatedAtDesc(tenantId, pacienteId);
		}
		Set<Long> pacientesComTratamentoAberto = repository.findByTenantIdOrderByDataRealizacaoDescCreatedAtDesc(tenantId)
				.stream()
				.filter(item -> !Boolean.TRUE.equals(item.getFinalizado()))
				.map(TratamentoRealizadoEntity::getPacienteId)
				.collect(Collectors.toSet());
		pacientesComTratamentoAberto.forEach(id -> sincronizarSaldoAvaliacaoAtual(tenantId, id));
		return repository.findByTenantIdOrderByDataRealizacaoDescCreatedAtDesc(tenantId);
	}

	@Transactional
	public TratamentoRealizadoEntity criar(Long tenantId, TratamentoRealizadoRequestDTO dto) {
		TratamentoRealizadoEntity tratamento = new TratamentoRealizadoEntity();
		tratamento.setTenantId(tenantId);
		preencherTratamento(tenantId, tratamento, dto);

		TratamentoRealizadoEntity salvo = repository.save(tratamento);
		sincronizarSaldoAvaliacaoAtual(tenantId, salvo.getPacienteId());
		return salvo;
	}

	@Transactional
	public TratamentoRealizadoEntity atualizar(Long tenantId, Long id, TratamentoRealizadoRequestDTO dto) {
		TratamentoRealizadoEntity tratamento = repository.findById(id).orElseThrow();
		if (!tratamento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Tratamento nao encontrado.");
		}

		preencherTratamento(tenantId, tratamento, dto);
		TratamentoRealizadoEntity salvo = repository.save(tratamento);
		sincronizarSaldoAvaliacaoAtual(tenantId, salvo.getPacienteId());
		return salvo;
	}

	@Transactional
	public void excluir(Long tenantId, Long id) {
		TratamentoRealizadoEntity tratamento = repository.findById(id).orElseThrow();
		if (!tratamento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Tratamento nao encontrado.");
		}

		Long pacienteId = tratamento.getPacienteId();
		repository.delete(tratamento);
		sincronizarSaldoAvaliacaoAtual(tenantId, pacienteId);
	}

	@Transactional
	public TratamentoRealizadoEntity finalizar(Long tenantId, Long id) {
		TratamentoRealizadoEntity tratamento = repository.findById(id).orElseThrow();
		if (!tratamento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Tratamento nao encontrado.");
		}
		if (Boolean.TRUE.equals(tratamento.getFinalizado())) {
			return tratamento;
		}

		List<TratamentoRealizadoEntity> tratamentosAtivos = tratamentosAbertosDaAvaliacaoAtual(tenantId,
				tratamento.getPacienteId(), null);
		if (tratamentosAtivos.isEmpty()) {
			throw new RuntimeException("Nao ha tratamento em aberto para finalizar.");
		}
		BigDecimal saldoAtual = calcularSaldoAvaliacao(tratamentosAtivos, BigDecimal.ZERO, BigDecimal.ZERO);
		if (saldoAtual.compareTo(BigDecimal.ZERO) > 0) {
			throw new RuntimeException("O tratamento so pode ser finalizado quando o saldo estiver zerado.");
		}

		LocalDateTime dataFinalizacao = LocalDateTime.now();
		tratamentosAtivos.forEach(item -> {
			item.setFinalizado(true);
			item.setDataFinalizacao(dataFinalizacao);
		});
		repository.saveAll(tratamentosAtivos);
		return tratamento;
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
		List<TratamentoRealizadoEntity> tratamentosAtivos = tratamentosAbertosDaAvaliacaoAtual(tenantId, dto.getPacienteId(),
				tratamento.getId());
		BigDecimal valorTotalAvaliacao = valorTotalAvaliacao(tratamentosAtivos, valorTotal);
		BigDecimal totalPagoOutros = totalPago(tratamentosAtivos);
		BigDecimal saldoDisponivel = valorTotalAvaliacao.subtract(totalPagoOutros);
		if (dto.getValorPago().compareTo(saldoDisponivel) > 0) {
			throw new RuntimeException("Valor pago nao pode ser maior que o saldo em aberto da avaliacao.");
		}
		BigDecimal saldoAtual = saldoDisponivel.subtract(dto.getValorPago());

		tratamento.setPacienteId(dto.getPacienteId());
		tratamento.setProcedimentoId(dto.getProcedimentoId());
		tratamento.setTratamento(resolveTratamento(dto));
		tratamento.setDente(normalizarDente(dto.getDente()));
		tratamento.setValorPago(dto.getValorPago());
		tratamento.setValorTratamento(valorTratamento);
		tratamento.setValorTotal(valorTotalAvaliacao);
		tratamento.setSaldo(saldoAtual);
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

	private List<TratamentoRealizadoEntity> tratamentosAbertosDaAvaliacaoAtual(Long tenantId, Long pacienteId,
			Long tratamentoIdExcluido) {
		List<TratamentoRealizadoEntity> tratamentosPaciente = repository
				.findByTenantIdAndPacienteIdOrderByDataRealizacaoDescCreatedAtDesc(tenantId, pacienteId);

		return tratamentosPaciente.stream()
				.filter(item -> tratamentoIdExcluido == null || !item.getId().equals(tratamentoIdExcluido))
				.filter(item -> !Boolean.TRUE.equals(item.getFinalizado()))
				.toList();
	}

	private void sincronizarSaldoAvaliacaoAtual(Long tenantId, Long pacienteId) {
		List<TratamentoRealizadoEntity> tratamentosAtivos = tratamentosAbertosDaAvaliacaoAtual(tenantId, pacienteId, null);
		if (tratamentosAtivos.isEmpty()) {
			return;
		}

		BigDecimal valorTotalAvaliacao = valorTotalAvaliacao(tratamentosAtivos, BigDecimal.ZERO);
		BigDecimal saldoAtual = calcularSaldoAvaliacao(tratamentosAtivos, valorTotalAvaliacao, BigDecimal.ZERO);
		tratamentosAtivos.forEach(item -> {
			item.setValorTotal(valorTotalAvaliacao);
			item.setSaldo(saldoAtual);
		});
		repository.saveAll(tratamentosAtivos);
	}

	private BigDecimal calcularSaldoAvaliacao(List<TratamentoRealizadoEntity> tratamentosAtivos,
			BigDecimal valorTotalAvaliacao, BigDecimal valorPagoAdicional) {
		BigDecimal totalPago = totalPago(tratamentosAtivos).add(valorPagoAdicional == null ? BigDecimal.ZERO : valorPagoAdicional);
		BigDecimal saldo = (valorTotalAvaliacao == null ? BigDecimal.ZERO : valorTotalAvaliacao).subtract(totalPago);
		return saldo.max(BigDecimal.ZERO);
	}

	private BigDecimal valorTotalAvaliacao(List<TratamentoRealizadoEntity> tratamentosAtivos, BigDecimal valorTotalPadrao) {
		List<BigDecimal> valores = new ArrayList<>();
		if (valorTotalPadrao != null) {
			valores.add(valorTotalPadrao);
		}
		tratamentosAtivos.stream()
				.map(TratamentoRealizadoEntity::getValorTotal)
				.filter(valor -> valor != null && valor.compareTo(BigDecimal.ZERO) > 0)
				.forEach(valores::add);
		return valores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
	}

	private BigDecimal totalPago(List<TratamentoRealizadoEntity> tratamentosAtivos) {
		return tratamentosAtivos.stream()
				.map(TratamentoRealizadoEntity::getValorPago)
				.filter(valor -> valor != null && valor.compareTo(BigDecimal.ZERO) > 0)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
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
