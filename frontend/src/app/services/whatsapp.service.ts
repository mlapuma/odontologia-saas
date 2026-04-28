import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface WhatsappRequest {
  telefone?: string;
  mensagem: string;
  tipo?: string;
  pacienteId?: number;
  agendamentoId?: number;
}

export interface WhatsappNotificacao {
  id: number;
  telefoneDestino: string;
  mensagem: string;
  status: 'PENDENTE' | 'ENVIADO' | 'ERRO' | string;
  resposta?: string;
}

@Injectable({
  providedIn: 'root'
})
export class WhatsappService {
  constructor(private http: HttpClient) {}

  enviar(request: WhatsappRequest): Observable<WhatsappNotificacao> | null {
    const telefone = this.normalizarTelefone(request.telefone);
    if (!telefone) {
      return null;
    }

    return this.http.post<WhatsappNotificacao>(`${environment.apiUrl}/whatsapp/enviar`, {
      ...request,
      telefone
    });
  }

  montarUrlManual(request: WhatsappRequest): string {
    const telefone = this.normalizarTelefone(request.telefone);
    if (!telefone) {
      return '';
    }
    return `whatsapp://send?phone=${telefone}&text=${encodeURIComponent(request.mensagem)}`;
  }

  abrirDesktop(request: WhatsappRequest): boolean {
    const url = this.montarUrlManual(request);
    if (!url) {
      return false;
    }

    const link = document.createElement('a');
    link.href = url;
    link.rel = 'noopener';
    document.body.appendChild(link);
    link.click();
    link.remove();
    return true;
  }

  normalizarTelefone(telefone?: string): string {
    const apenasNumeros = (telefone || '').replace(/\D/g, '');
    if (!apenasNumeros || apenasNumeros.length < 10) {
      return '';
    }
    if (apenasNumeros.startsWith('55')) {
      return apenasNumeros.length === 12 || apenasNumeros.length === 13 ? apenasNumeros : '';
    }
    if (apenasNumeros.length === 10 || apenasNumeros.length === 11) {
      return `55${apenasNumeros}`;
    }
    return apenasNumeros;
  }
}
