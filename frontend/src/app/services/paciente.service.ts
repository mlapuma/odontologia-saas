import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Paciente } from '../models/paciente.model';
import { environment } from '../../environments/environment';


@Injectable({
    providedIn: 'root'
})
export class PacienteService {

    private api = `${environment.apiUrl}/pacientes`;

    constructor(private http: HttpClient) { }

    listar(): Observable<Paciente[]> {
        return this.http.get<Paciente[]>(this.api);
    }

    buscarPorId(id: number): Observable<Paciente> {
        return this.http.get<Paciente>(`${this.api}/${id}`);
    }

    salvar(paciente: Paciente): Observable<Paciente> {
        return this.http.post<Paciente>(this.api, paciente);
    }

    atualizar(id: number, paciente: Paciente): Observable<Paciente> {
        return this.http.put<Paciente>(`${this.api}/${id}`, paciente);
    }

    excluir(id: number): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }
}