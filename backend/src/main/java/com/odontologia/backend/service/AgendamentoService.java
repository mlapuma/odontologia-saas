package com.odontologia.backend.service;

import com.odontologia.backend.dto.AgendaEventoDTO;
import com.odontologia.backend.dto.AgendamentoDetalheResponseDTO;
import com.odontologia.backend.dto.AgendamentoProcedimentoDTO;
import com.odontologia.backend.dto.AgendamentoRequestDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.entity.AgendamentoProcedimentoEntity;
import com.odontologia.backend.entity.ProcedimentoEntity;
import com.odontologia.backend.repository.AgendamentoProcedimentoRepository;
import com.odontologia.backend.repository.AgendamentoRepository;
import com.odontologia.backend.repository.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgendamentoService {

	private final AgendamentoRepository repository;
	private final ProcedimentoRepository procedimentoRepository;
	private final AgendamentoProcedimentoRepository agendamentoProcedimentoRepository;

	public AgendamentoService(AgendamentoRepository repository, ProcedimentoRepository procedimentoRepository,
			AgendamentoProcedimentoRepository agendamentoProcedimentoRepository) {
		this.repository = repository;
		this.procedimentoRepository = procedimentoRepository;
		this.agendamentoProcedimentoRepository = agendamentoProcedimentoRepository;
	}

	@Transactional
	public AgendamentoDetalheResponseDTO criar(AgendamentoRequestDTO dto) {
		if (dto.getProcedimentos() == null || dto.getProcedimentos().isEmpty()) {
			throw new RuntimeException("É obrigatório informar ao menos um procedimento.");
		}

		ResumoAgendamento resumo = calcularResumo(dto.getProcedimentos());

		LocalDateTime inicio = dto.getDataHoraInicio();
		LocalDateTime fim = inicio.plusMinutes(resumo.duracaoTotal());

		validarConflito(dto.getTenantId(), dto.getProfissionalId(), inicio, fim);

		AgendamentoEntity entity = new AgendamentoEntity();
		entity.setTenantId(dto.getTenantId());
		entity.setPacienteId(dto.getPacienteId());
		entity.setProfissionalId(dto.getProfissionalId());
		entity.setTabelaPrecoId(dto.getTabelaPrecoId());
		entity.setDataHoraInicio(inicio);
		entity.setDataHoraFim(fim);
		entity.setObservacoes(dto.getObservacoes());
		entity.setStatus("AGENDADO");
		entity.setConfirmadoWhatsapp(false);

		AgendamentoEntity salvo = repository.save(entity);

		for (AgendamentoProcedimentoDTO item : dto.getProcedimentos()) {
			ProcedimentoEntity procedimento = procedimentoRepository.findById(item.getProcedimentoId()).orElseThrow();

			AgendamentoProcedimentoEntity ap = new AgendamentoProcedimentoEntity();
			ap.setAgendamentoId(salvo.getId());
			ap.setProcedimentoId(procedimento.getId());
			ap.setQuantidade(item.getQuantidade() == null ? 1 : item.getQuantidade());
			ap.setValorCobrado(procedimento.getValorBase());

			agendamentoProcedimentoRepository.save(ap);
		}

		return montarDetalhe(salvo, resumo.valorTotal(), resumo.duracaoTotal(), dto.getProcedimentos());
	}

	@Transactional
	public AgendamentoDetalheResponseDTO atualizar(Long id, AgendamentoRequestDTO dto) {
		if (dto.getProcedimentos() == null || dto.getProcedimentos().isEmpty()) {
			throw new RuntimeException("É obrigatório informar ao menos um procedimento.");
		}

		AgendamentoEntity entity = repository.findById(id).orElseThrow();
		ResumoAgendamento resumo = calcularResumo(dto.getProcedimentos());
		LocalDateTime inicio = dto.getDataHoraInicio();
		LocalDateTime fim = inicio.plusMinutes(resumo.duracaoTotal());

		validarConflito(entity.getTenantId(), dto.getProfissionalId(), id, inicio, fim);

		entity.setPacienteId(dto.getPacienteId());
		entity.setProfissionalId(dto.getProfissionalId());
		entity.setTabelaPrecoId(dto.getTabelaPrecoId());
		entity.setDataHoraInicio(inicio);
		entity.setDataHoraFim(fim);
		entity.setObservacoes(dto.getObservacoes());

		AgendamentoEntity salvo = repository.save(entity);

		agendamentoProcedimentoRepository.deleteByAgendamentoId(id);
		for (AgendamentoProcedimentoDTO item : dto.getProcedimentos()) {
			ProcedimentoEntity procedimento = procedimentoRepository.findById(item.getProcedimentoId()).orElseThrow();

			AgendamentoProcedimentoEntity ap = new AgendamentoProcedimentoEntity();
			ap.setAgendamentoId(salvo.getId());
			ap.setProcedimentoId(procedimento.getId());
			ap.setQuantidade(item.getQuantidade() == null ? 1 : item.getQuantidade());
			ap.setValorCobrado(procedimento.getValorBase());

			agendamentoProcedimentoRepository.save(ap);
		}

		return montarDetalhe(salvo, resumo.valorTotal(), resumo.duracaoTotal(), dto.getProcedimentos());
	}

	public List<AgendamentoEntity> listarPorProfissional(Long tenantId, Long profissionalId) {
		return repository.findByTenantIdAndProfissionalIdOrderByDataHoraInicioAsc(tenantId, profissionalId);
	}

	public List<AgendamentoEntity> listarPorDia(Long tenantId, LocalDate data) {
		LocalDateTime inicio = data.atStartOfDay();
		LocalDateTime fim = data.atTime(LocalTime.MAX);
		return repository.findByTenantIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(tenantId, inicio, fim);
	}

	@Transactional
	public AgendamentoEntity confirmarWhatsapp(Long id) {
		AgendamentoEntity entity = repository.findById(id).orElseThrow();
		entity.setConfirmadoWhatsapp(true);
		entity.setDataConfirmacaoWhatsapp(LocalDateTime.now());
		entity.setStatus("CONFIRMADO");
		return repository.save(entity);
	}

	public AgendamentoDetalheResponseDTO detalhar(Long id) {
		AgendamentoEntity entity = repository.findById(id).orElseThrow();
		List<AgendamentoProcedimentoEntity> itens = agendamentoProcedimentoRepository.findByAgendamentoId(id);

		BigDecimal valorTotal = BigDecimal.ZERO;
		Integer duracaoTotal = 0;
		List<AgendamentoProcedimentoDTO> procedimentos = new ArrayList<>();

		for (AgendamentoProcedimentoEntity item : itens) {
			ProcedimentoEntity procedimento = procedimentoRepository.findById(item.getProcedimentoId()).orElseThrow();

			valorTotal = valorTotal.add(item.getValorCobrado().multiply(BigDecimal.valueOf(item.getQuantidade())));
			duracaoTotal += procedimento.getDuracaoMinutos() * item.getQuantidade();

			AgendamentoProcedimentoDTO dto = new AgendamentoProcedimentoDTO();
			dto.setProcedimentoId(item.getProcedimentoId());
			dto.setQuantidade(item.getQuantidade());
			procedimentos.add(dto);
		}

		AgendamentoDetalheResponseDTO response = new AgendamentoDetalheResponseDTO();
		response.setId(entity.getId());
		response.setTenantId(entity.getTenantId());
		response.setPacienteId(entity.getPacienteId());
		response.setProfissionalId(entity.getProfissionalId());
		response.setDataHoraInicio(entity.getDataHoraInicio());
		response.setDataHoraFim(entity.getDataHoraFim());
		response.setStatus(entity.getStatus());
		response.setObservacoes(entity.getObservacoes());
		response.setConfirmadoWhatsapp(entity.getConfirmadoWhatsapp());
		response.setValorTotal(valorTotal);
		response.setDuracaoTotalMinutos(duracaoTotal);
		response.setProcedimentos(procedimentos);

		return response;
	}

	private AgendamentoDetalheResponseDTO montarDetalhe(AgendamentoEntity entity, BigDecimal valorTotal,
			Integer duracaoTotal, List<AgendamentoProcedimentoDTO> procedimentos) {
		AgendamentoDetalheResponseDTO response = new AgendamentoDetalheResponseDTO();
		response.setId(entity.getId());
		response.setTenantId(entity.getTenantId());
		response.setPacienteId(entity.getPacienteId());
		response.setProfissionalId(entity.getProfissionalId());
		response.setDataHoraInicio(entity.getDataHoraInicio());
		response.setDataHoraFim(entity.getDataHoraFim());
		response.setStatus(entity.getStatus());
		response.setObservacoes(entity.getObservacoes());
		response.setConfirmadoWhatsapp(entity.getConfirmadoWhatsapp());
		response.setValorTotal(valorTotal);
		response.setDuracaoTotalMinutos(duracaoTotal);
		response.setProcedimentos(procedimentos);
		return response;
	}

	private void validarConflito(Long tenantId, Long profissionalId, LocalDateTime inicio, LocalDateTime fim) {
		List<AgendamentoEntity> conflitos = repository.buscarConflitos(tenantId, profissionalId, inicio, fim);
		if (!conflitos.isEmpty()) {
			throw new RuntimeException("Já existe agendamento para este profissional no horário informado.");
		}
	}

	private void validarConflito(Long tenantId, Long profissionalId, Long agendamentoId, LocalDateTime inicio,
			LocalDateTime fim) {
		List<AgendamentoEntity> conflitos = repository.buscarConflitosIgnorandoAgendamento(tenantId, profissionalId,
				agendamentoId, inicio, fim);
		if (!conflitos.isEmpty()) {
			throw new RuntimeException("Já existe agendamento para este profissional no horário informado.");
		}
	}

	private ResumoAgendamento calcularResumo(List<AgendamentoProcedimentoDTO> procedimentos) {
		Integer duracaoTotal = 0;
		BigDecimal valorTotal = BigDecimal.ZERO;

		for (AgendamentoProcedimentoDTO item : procedimentos) {
			ProcedimentoEntity procedimento = procedimentoRepository.findById(item.getProcedimentoId()).orElseThrow();
			int quantidade = item.getQuantidade() == null ? 1 : item.getQuantidade();

			duracaoTotal += procedimento.getDuracaoMinutos() * quantidade;
			valorTotal = valorTotal.add(procedimento.getValorBase().multiply(BigDecimal.valueOf(quantidade)));
		}

		return new ResumoAgendamento(valorTotal, duracaoTotal);
	}

	private record ResumoAgendamento(BigDecimal valorTotal, Integer duracaoTotal) {
	}

	public List<AgendaEventoDTO> listarEventosCalendario(Long tenantId, LocalDateTime inicio, LocalDateTime fim) {
		List<AgendamentoEntity> agendamentos = repository
				.findByTenantIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(tenantId, inicio, fim);

		List<AgendaEventoDTO> eventos = new ArrayList<>();

		for (AgendamentoEntity agendamento : agendamentos) {
			AgendaEventoDTO dto = new AgendaEventoDTO();
			dto.setId(agendamento.getId());
			dto.setTitle("Consulta #" + agendamento.getId());
			dto.setStart(agendamento.getDataHoraInicio());
			dto.setEnd(agendamento.getDataHoraFim());
			dto.setStatus(agendamento.getStatus());
			dto.setPacienteId(agendamento.getPacienteId());
			dto.setProfissionalId(agendamento.getProfissionalId());
			dto.setColor(definirCor(agendamento.getStatus()));

			eventos.add(dto);
		}

		return eventos;
	}

	private String definirCor(String status) {
		if (status == null)
			return "#3788d8";

		return switch (status.toUpperCase()) {
		case "CONFIRMADO" -> "#16a34a";
		case "CANCELADO" -> "#dc2626";
		case "REALIZADO" -> "#6b7280";
		case "FALTOU" -> "#ea580c";
		default -> "#2563eb";
		};
	}
}
