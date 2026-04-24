package com.odontologia.backend.service;

import com.odontologia.backend.dto.AgendamentoDetalheResponseDTO;
import com.odontologia.backend.dto.PagamentoRequestDTO;
import com.odontologia.backend.dto.RecebimentoDTO;
import com.odontologia.backend.entity.PagamentoEntity;
import com.odontologia.backend.repository.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagamentoService {

	private final PagamentoRepository repository;
	private final AgendamentoService agendamentoService;

	public PagamentoService(PagamentoRepository repository, AgendamentoService agendamentoService) {
		this.repository = repository;
		this.agendamentoService = agendamentoService;
	}

	@Transactional
	public PagamentoEntity gerarCobranca(PagamentoRequestDTO dto) {
		AgendamentoDetalheResponseDTO agendamento = agendamentoService.detalhar(dto.getAgendamentoId());

		PagamentoEntity pagamento = new PagamentoEntity();
		pagamento.setTenantId(dto.getTenantId());
		pagamento.setPacienteId(dto.getPacienteId());
		pagamento.setValorTotal(agendamento.getValorTotal());
		pagamento.setValorPago(BigDecimal.ZERO);
		pagamento.setStatus("PENDENTE");
		pagamento.setObservacoes(dto.getObservacoes());

		return repository.save(pagamento);
	}

	public List<PagamentoEntity> listar(Long tenantId) {
		return repository.findByTenantIdOrderByCreatedAtDesc(tenantId);
	}

	public List<PagamentoEntity> listarPendentes(Long tenantId) {
		return repository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, "PENDENTE");
	}

	@Transactional
	public PagamentoEntity receber(Long pagamentoId, RecebimentoDTO dto) {
		PagamentoEntity pagamento = repository.findById(pagamentoId).orElseThrow();

		pagamento.setValorPago(dto.getValorPago());
		pagamento.setFormaPagamento(dto.getFormaPagamento());
		pagamento.setDataPagamento(LocalDateTime.now());
		pagamento.setObservacoes(dto.getObservacoes());

		if (dto.getValorPago().compareTo(pagamento.getValorTotal()) >= 0) {
			pagamento.setStatus("PAGO");
		} else if (dto.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
			pagamento.setStatus("PARCIAL");
		} else {
			pagamento.setStatus("PENDENTE");
		}

		return repository.save(pagamento);
	}
}