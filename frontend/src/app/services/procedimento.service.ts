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
}
