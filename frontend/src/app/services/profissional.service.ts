import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Profissional } from '../models/profissional.model';

@Injectable({
  providedIn: 'root'
})
export class ProfissionalService {
  private api = `${environment.apiUrl}/profissionais`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Profissional[]> {
    return this.http.get<Profissional[]>(this.api);
  }

  salvar(profissional: Profissional): Observable<Profissional> {
    return this.http.post<Profissional>(this.api, profissional);
  }

  atualizar(id: number, profissional: Profissional): Observable<Profissional> {
    return this.http.put<Profissional>(`${this.api}/${id}`, profissional);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
