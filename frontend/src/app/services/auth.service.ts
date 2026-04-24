import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from '../models/login-response.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor(private http: HttpClient) { }

    login(email: string, senha: string): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, senha }).pipe(
            tap(response => {
                localStorage.setItem('token', response.token);
                localStorage.setItem('usuarioNome', response.nome);
                localStorage.setItem('usuarioEmail', response.email);
                localStorage.setItem('tenantId', String(response.tenantId));
                localStorage.setItem('perfil', response.perfil);
            })
        );
    }

    logout(): void {
        localStorage.clear();
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}