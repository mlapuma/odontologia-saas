import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PacienteReativacao, TratamentoRealizado } from '../models/tratamento-realizado.model';

@Injectable({
  providedIn: 'root'
})
export class TratamentoRealizadoService {

  private api = `${environment.apiUrl}/tratamentos-realizados`;

  constructor(private http: HttpClient) { }

  listar(pacienteId?: number): Observable<TratamentoRealizado[]> {
    let params = new HttpParams();
    if (pacienteId) {
      params = params.set('pacienteId', pacienteId);
    }
    return this.http.get<TratamentoRealizado[]>(this.api, { params });
  }

  salvar(tratamento: TratamentoRealizado): Observable<TratamentoRealizado> {
    return this.http.post<TratamentoRealizado>(this.api, tratamento);
  }

  pacientesParaReativacao(diasSemComparecer: number): Observable<PacienteReativacao[]> {
    const params = new HttpParams().set('diasSemComparecer', diasSemComparecer);
    return this.http.get<PacienteReativacao[]>(`${this.api}/pacientes-reativacao`, { params });
  }
}
