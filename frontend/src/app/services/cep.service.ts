import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface CepResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CepService {
  constructor(private http: HttpClient) {}

  consultar(cep: string): Observable<CepResponse> {
    const cepLimpo = cep.replace(/\D/g, '');
    return this.http.get<CepResponse>(`https://viacep.com.br/ws/${cepLimpo}/json/`);
  }
}
