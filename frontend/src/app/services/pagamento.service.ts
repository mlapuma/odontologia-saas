import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FinanceiroResumo, Pagamento } from '../models/pagamento.model';

@Injectable({
  providedIn: 'root'
})
export class PagamentoService {
  private api = `${environment.apiUrl}/pagamentos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Pagamento[]> {
    return this.http.get<Pagamento[]>(this.api, { params: this.tenantParams() });
  }

  listarPendentes(): Observable<Pagamento[]> {
    return this.http.get<Pagamento[]>(`${this.api}/pendentes`, { params: this.tenantParams() });
  }

  resumo(): Observable<FinanceiroResumo> {
    return this.http.get<FinanceiroResumo>(`${this.api}/resumo`, { params: this.tenantParams() });
  }

  private tenantParams(): HttpParams {
    return new HttpParams().set('tenantId', localStorage.getItem('tenantId') || '1');
  }
}
