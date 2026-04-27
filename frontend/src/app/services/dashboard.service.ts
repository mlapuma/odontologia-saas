import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardResumo } from '../models/dashboard-resumo.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  constructor(private http: HttpClient) {}

  resumo(): Observable<DashboardResumo> {
    return this.http.get<DashboardResumo>(`${environment.apiUrl}/dashboard`);
  }
}
