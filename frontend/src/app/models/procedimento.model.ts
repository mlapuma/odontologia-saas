export interface Procedimento {
  id: number;
  tenantId?: number;
  nome: string;
  descricao?: string;
  categoria?: string;
  duracaoMinutos: number;
  valorBase: number;
  ativo?: boolean;
}
