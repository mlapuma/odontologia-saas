import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AgendaEvento } from '../models/agenda-evento.model';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AgendaService {

  private api = `${environment.apiUrl}/agendamentos`;

  constructor(private http: HttpClient) {}

  listarCalendario(inicio: string, fim: string): Observable<AgendaEvento[]> {
    return this.http.get<AgendaEvento[]>(`${this.api}/calendario?inicio=${inicio}&fim=${fim}`);
  }
}