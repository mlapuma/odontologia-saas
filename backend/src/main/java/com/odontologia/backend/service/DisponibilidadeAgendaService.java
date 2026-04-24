package com.odontologia.backend.service;

import com.odontologia.backend.dto.HorarioDisponivelDTO;
import com.odontologia.backend.entity.AgendamentoEntity;
import com.odontologia.backend.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadeAgendaService {

	private static final LocalTime HORA_INICIO_CLINICA = LocalTime.of(8, 0);
	private static final LocalTime HORA_FIM_CLINICA = LocalTime.of(18, 0);
	private static final int INTERVALO_GRADE_MINUTOS = 30;

	private final AgendamentoRepository agendamentoRepository;

	public DisponibilidadeAgendaService(AgendamentoRepository agendamentoRepository) {
		this.agendamentoRepository = agendamentoRepository;
	}

	public List<HorarioDisponivelDTO> buscarHorariosDisponiveis(Long tenantId, Long profissionalId, LocalDate data,
			Integer duracaoMinutos) {

		LocalDateTime inicioDia = data.atTime(HORA_INICIO_CLINICA);
		LocalDateTime fimDia = data.atTime(HORA_FIM_CLINICA);

		List<AgendamentoEntity> agendamentos = agendamentoRepository
				.findByTenantIdAndProfissionalIdAndDataHoraInicioBetweenOrderByDataHoraInicioAsc(tenantId,
						profissionalId, inicioDia, fimDia);

		List<HorarioDisponivelDTO> horarios = new ArrayList<>();

		LocalDateTime cursor = inicioDia;

		while (!cursor.plusMinutes(duracaoMinutos).isAfter(fimDia)) {
			LocalDateTime fimSlot = cursor.plusMinutes(duracaoMinutos);

			boolean disponivel = !existeConflito(cursor, fimSlot, agendamentos);

			horarios.add(new HorarioDisponivelDTO(cursor, fimSlot, disponivel));

			cursor = cursor.plusMinutes(INTERVALO_GRADE_MINUTOS);
		}

		return horarios;
	}

	private boolean existeConflito(LocalDateTime novoInicio, LocalDateTime novoFim,
			List<AgendamentoEntity> agendamentos) {

		for (AgendamentoEntity agendamento : agendamentos) {
			if ("CANCELADO".equalsIgnoreCase(agendamento.getStatus())) {
				continue;
			}

			boolean conflito = novoInicio.isBefore(agendamento.getDataHoraFim())
					&& novoFim.isAfter(agendamento.getDataHoraInicio());

			if (conflito) {
				return true;
			}
		}

		return false;
	}
}