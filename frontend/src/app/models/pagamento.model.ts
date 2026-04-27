export interface Pagamento {
  id: number;
  tenantId: number;
  atendimentoId?: number;
  pacienteId: number;
  valorTotal: number;
  valorPago: number;
  formaPagamento?: string;
  status: string;
  dataPagamento?: string;
  observacoes?: string;
}

export interface FinanceiroResumo {
  totalRecebidoHoje: number;
  totalPendente: number;
  quantidadePendencias: number;
  quantidadePagosHoje: number;
}
