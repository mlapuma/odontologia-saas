import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';

interface ProximoAgendamento {
  id: number;
  pacienteNome: string;
  dataHoraInicio: string;
  status: string;
}

interface PacienteSemRetorno {
  pacienteId: number;
  nome: string;
  whatsapp: string;
  ultimaDataTratamento: string;
}

interface DashboardResumo {
  pacientesTotal: number;
  pacientesAtivos: number;
  pacientesSemRetorno: number;
  agendamentosHoje: number;
  consultasConfirmadasHoje: number;
  consultasCanceladasHoje: number;
  mensagensWhatsappHoje: number;
  tratamentosRealizadosMes: number;
  faturamentoPrevistoHoje: number;
  valorRecebidoMes: number;
  proximosAgendamentos: ProximoAgendamento[];
  pacientesParaReativar: PacienteSemRetorno[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  resumo?: DashboardResumo;
  carregando = false;
  erro = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.carregarDashboard();
  }

  private carregarDashboard(): void {
    this.carregando = true;
    this.erro = '';

    this.http.get<DashboardResumo>(`${environment.apiUrl}/dashboard`).subscribe({
      next: (response) => {
        this.resumo = response;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar o dashboard.';
        this.carregando = false;
      }
    });
  }
}
