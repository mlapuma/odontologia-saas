package com.odontologia.backend.service;

import com.odontologia.backend.entity.ProcedimentoEntity;
import com.odontologia.backend.repository.ProcedimentoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProcedimentoPadraoService {

	private static final List<String> PROCEDIMENTOS_ODONTOLOGICOS = List.of(
			"Avaliação odontológica",
			"Profilaxia / limpeza",
			"Aplicação de flúor",
			"Raspagem periodontal",
			"Tratamento periodontal",
			"Clareamento dental",
			"Restauração em resina",
			"Restauração em amálgama",
			"Remocao de carie + curativo",
			"Exo",
			"Medicacao",
			"Moldagem",
			"Tratamento de canal",
			"Retratamento de canal",
			"Extração dentária",
			"Extração de siso",
			"Cirurgia oral menor",
			"Implante dentário",
			"Protocolo sobre implantes",
			"Próteses fixas",
			"Prótese removível",
			"Prótese total",
			"Coroa dentária",
			"Lente de contato dental",
			"Faceta em resina",
			"Faceta em porcelana",
			"Aparelho ortodôntico",
			"Manutenção ortodôntica",
			"Alinhadores transparentes",
			"Tratamento de bruxismo",
			"Placa miorrelaxante",
			"Odontopediatria",
			"Selante dental",
			"Radiografia odontológica",
			"Enxerto ósseo",
			"Gengivoplastia",
			"Frenectomia",
			"Urgência odontológica"
	);

	private final ProcedimentoRepository repository;

	public ProcedimentoPadraoService(ProcedimentoRepository repository) {
		this.repository = repository;
	}

	public List<ProcedimentoEntity> listarComPadroes(Long tenantId) {
		garantirPadroes(tenantId);
		return repository.findByTenantId(tenantId);
	}

	public ProcedimentoEntity atualizarValor(Long tenantId, Long id, BigDecimal valorBase) {
		if (valorBase == null || valorBase.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Valor do procedimento deve ser maior ou igual a zero.");
		}

		ProcedimentoEntity procedimento = repository.findById(id).orElseThrow();
		if (!procedimento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Procedimento nao encontrado.");
		}

		procedimento.setValorBase(valorBase);
		return repository.save(procedimento);
	}

	public ProcedimentoEntity salvar(Long tenantId, ProcedimentoRequest request) {
		String nome = normalizarNome(request.nome());
		BigDecimal valorBase = normalizarValor(request.valorBase());
		ProcedimentoEntity procedimento = request.id() == null
				? new ProcedimentoEntity()
				: repository.findById(request.id()).orElseThrow();

		if (procedimento.getId() == null) {
			if (!repository.findByTenantIdAndNomeIgnoreCaseOrderByAtivoDescIdAsc(tenantId, nome).isEmpty()) {
				throw new RuntimeException("Ja existe um procedimento com este nome.");
			}
			procedimento.setTenantId(tenantId);
			procedimento.setAtivo(true);
			procedimento.setDuracaoMinutos(45);
			procedimento.setCategoria("Odontologia");
		} else if (!procedimento.getTenantId().equals(tenantId)) {
			throw new RuntimeException("Procedimento nao encontrado.");
		}

		procedimento.setNome(nome);
		procedimento.setValorBase(valorBase);
		if (request.categoria() != null && !request.categoria().trim().isEmpty()) {
			procedimento.setCategoria(request.categoria().trim());
		}
		return repository.save(procedimento);
	}

	private String normalizarNome(String nome) {
		if (nome == null || nome.trim().isEmpty()) {
			throw new RuntimeException("Nome do procedimento e obrigatorio.");
		}
		return nome.trim();
	}

	private BigDecimal normalizarValor(BigDecimal valorBase) {
		BigDecimal valor = valorBase == null ? BigDecimal.ZERO : valorBase;
		if (valor.compareTo(BigDecimal.ZERO) < 0) {
			throw new RuntimeException("Valor do procedimento deve ser maior ou igual a zero.");
		}
		return valor;
	}

	public record ProcedimentoRequest(Long id, String nome, BigDecimal valorBase, String categoria) {
	}

	private void garantirPadroes(Long tenantId) {
		for (String nome : PROCEDIMENTOS_ODONTOLOGICOS) {
			if (!repository.findByTenantIdAndNomeIgnoreCaseOrderByAtivoDescIdAsc(tenantId, nome).isEmpty()) {
				continue;
			}

			ProcedimentoEntity procedimento = new ProcedimentoEntity();
			procedimento.setTenantId(tenantId);
			procedimento.setNome(nome);
			procedimento.setCategoria("Odontologia");
			procedimento.setDuracaoMinutos(45);
			procedimento.setValorBase(BigDecimal.ZERO);
			procedimento.setAtivo(true);
			repository.save(procedimento);
		}
	}
}
