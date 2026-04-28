export interface TratamentoRealizado {
  id?: number;
  tenantId?: number;
  pacienteId: number;
  procedimentoId?: number | null;
  tratamento: string;
  dente?: string | null;
  valorTratamento?: number;
  valorTotal: number;
  valorPago: number;
  saldo?: number;
  formaPagamento?: string | null;
  parcelas?: number | null;
  dataRealizacao: string;
  finalizado?: boolean;
  dataFinalizacao?: string | null;
  observacoes?: string;
}

export interface PacienteReativacao {
  pacienteId: number;
  nome: string;
  whatsapp: string;
  ultimaDataTratamento: string;
  totalPago: number;
}
