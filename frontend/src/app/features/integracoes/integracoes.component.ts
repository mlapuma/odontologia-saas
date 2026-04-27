import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';

interface GoogleBusinessProfileConfig {
  configurado: boolean;
  clientIdConfigurado: boolean;
  clientSecretConfigurado: boolean;
  redirectUri: string;
  authorizationUrl?: string;
  autorizado: boolean;
  mensagem: string;
  escopos: string[];
}

@Component({
  selector: 'app-integracoes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './integracoes.component.html',
  styleUrl: './integracoes.component.css'
})
export class IntegracoesComponent implements OnInit {
  google?: GoogleBusinessProfileConfig;
  carregando = false;
  erro = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.carregarGoogleBusinessProfile();
  }

  carregarGoogleBusinessProfile(): void {
    this.carregando = true;
    this.erro = '';

    this.http.get<GoogleBusinessProfileConfig>(`${environment.apiUrl}/integracoes/google-business-profile`).subscribe({
      next: (response) => {
        this.google = response;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar status da integração com Google.';
        this.carregando = false;
      }
    });
  }

  conectarGoogle(): void {
    if (!this.google?.authorizationUrl) {
      return;
    }

    window.location.href = this.google.authorizationUrl;
  }
}
