export interface Paciente {

    id?: number;
    tenantId?: number;
    nome: string;
    cpf?: string;
    telefone?: string;
    whatsapp?: string;
    email?: string;
    dataNascimento?: string;
    cidade?: string;
    uf?: string;
    ativo?: boolean;
}