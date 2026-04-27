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

	private void garantirPadroes(Long tenantId) {
		for (String nome : PROCEDIMENTOS_ODONTOLOGICOS) {
			if (repository.findByTenantIdAndNomeIgnoreCase(tenantId, nome).isPresent()) {
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
