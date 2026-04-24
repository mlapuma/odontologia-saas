export interface AgendamentoProcedimento {
  procedimentoId: number;
  quantidade: number;
}

export interface AgendamentoRequest {
  tenantId: number;
  pacienteId: number;
  profissionalId: number;
  tabelaPrecoId?: number | null;
  dataHoraInicio: string;
  observacoes?: string;
  procedimentos: AgendamentoProcedimento[];
}
