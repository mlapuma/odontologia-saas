import { AgendamentoProcedimento } from './agendamento-request-model';

export interface AgendamentoDetalhe {
  id: number;
  tenantId: number;
  pacienteId: number;
  profissionalId: number;
  dataHoraInicio: string;
  dataHoraFim: string;
  status: string;
  observacoes?: string;
  confirmadoWhatsapp: boolean;
  valorTotal: number;
  duracaoTotalMinutos: number;
  procedimentos: AgendamentoProcedimento[];
}
