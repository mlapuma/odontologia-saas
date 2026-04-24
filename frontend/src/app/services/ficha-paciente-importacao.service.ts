import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FichaPacientePreview } from '../models/ficha-paciente-preview.model';
import { Paciente } from '../models/paciente.model';

@Injectable({
  providedIn: 'root'
})
export class FichaPacienteImportacaoService {

  private api = `${environment.apiUrl}/pacientes/importacao-ficha`;

  constructor(private http: HttpClient) { }

  preview(arquivo: File): Observable<FichaPacientePreview> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<FichaPacientePreview>(`${this.api}/preview`, formData);
  }

  salvar(paciente: Paciente): Observable<Paciente> {
    return this.http.post<Paciente>(`${this.api}/salvar`, paciente);
  }
}
