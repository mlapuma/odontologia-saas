import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Procedimento } from '../models/procedimento.model';

@Injectable({
  providedIn: 'root'
})
export class ProcedimentoService {

  private api = `${environment.apiUrl}/procedimentos`;

  constructor(private http: HttpClient) { }

  listar(): Observable<Procedimento[]> {
    return this.http.get<Procedimento[]>(this.api);
  }

  atualizarValor(id: number, valorBase: number): Observable<Procedimento> {
    return this.http.put<Procedimento>(`${this.api}/${id}/valor`, { valorBase });
  }

  salvar(procedimento: Partial<Procedimento>): Observable<Procedimento> {
    if (procedimento.id) {
      return this.http.put<Procedimento>(`${this.api}/${procedimento.id}`, procedimento);
    }
    return this.http.post<Procedimento>(this.api, procedimento);
  }
}
