export interface ProximoAgendamento {
  id: number;
  pacienteNome: string;
  dataHoraInicio: string;
  status: string;
}

export interface PacienteSemRetorno {
  pacienteId: number;
  nome: string;
  whatsapp: string;
  ultimaDataTratamento: string;
}

export interface TermoPesquisaGoogle {
  termo: string;
  impressoes: number;
}

export interface GoogleBusinessProfile {
  configurado: boolean;
  visualizacoesBusca: number;
  visualizacoesMaps: number;
  cliquesTelefone: number;
  cliquesSite: number;
  pedidosRota: number;
  periodo: string;
  localizacao?: string;
  mensagem: string;
  termosPesquisa: TermoPesquisaGoogle[];
}

export interface DashboardResumo {
  pacientesTotal: number;
  pacientesAtivos: number;
  pacientesSemRetorno: number;
  agendamentosHoje: number;
  consultasConfirmadasHoje: number;
  consultasCanceladasHoje: number;
  mensagensWhatsappHoje: number;
  tratamentosRealizadosMes: number;
  faturamentoPrevistoHoje: number;
  valorRecebidoMes: number;
  proximosAgendamentos: ProximoAgendamento[];
  pacientesParaReativar: PacienteSemRetorno[];
  googleBusinessProfile: GoogleBusinessProfile;
}
