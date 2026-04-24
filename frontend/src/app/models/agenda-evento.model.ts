export interface AgendaEvento {
    id: number;
    title: string;
    start: string;
    end: string;
    status: string;
    color: string;
    pacienteId: number;
    profissionalId: number;
}