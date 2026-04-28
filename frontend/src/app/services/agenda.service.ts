import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AgendaEvento } from '../models/agenda-evento.model';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { AgendamentoRequest } from '../models/agendamento-request-model';
import { AgendamentoDetalhe } from '../models/agendamento-detalhe.model';

@Injectable({
  providedIn: 'root'
})
export class AgendaService {

  private api = `${environment.apiUrl}/agendamentos`;

  constructor(private http: HttpClient) {}

  listarCalendario(inicio: string, fim: string): Observable<AgendaEvento[]> {
    return this.http.get<AgendaEvento[]>(`${this.api}/calendario?inicio=${inicio}&fim=${fim}`);
  }

  criar(agendamento: AgendamentoRequest): Observable<AgendamentoDetalhe> {
    return this.http.post<AgendamentoDetalhe>(this.api, agendamento);
  }

  atualizar(id: number, agendamento: AgendamentoRequest): Observable<AgendamentoDetalhe> {
    return this.http.put<AgendamentoDetalhe>(`${this.api}/${id}`, agendamento);
  }

  detalhar(id: number): Observable<AgendamentoDetalhe> {
    return this.http.get<AgendamentoDetalhe>(`${this.api}/${id}`);
  }
}
