export interface TratamentoRealizado {
  id?: number;
  tenantId?: number;
  pacienteId: number;
  procedimentoId?: number | null;
  tratamento: string;
  valorPago: number;
  dataRealizacao: string;
  observacoes?: string;
}

export interface PacienteReativacao {
  pacienteId: number;
  nome: string;
  whatsapp: string;
  ultimaDataTratamento: string;
  totalPago: number;
}
