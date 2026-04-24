import { Paciente } from './paciente.model';

export interface FichaPacientePreview {
  paciente: Paciente;
  textoExtraido?: string;
  aviso?: string;
}
