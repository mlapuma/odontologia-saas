import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardResumo } from '../../models/dashboard-resumo.model';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-proximos-agendamentos',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './proximos-agendamentos.component.html',
  styleUrl: './proximos-agendamentos.component.css'
})
export class ProximosAgendamentosComponent implements OnInit {
  resumo?: DashboardResumo;
  carregando = false;
  erro = '';

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = '';

    this.dashboardService.resumo().subscribe({
      next: (response) => {
        this.resumo = response;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar próximos agendamentos.';
        this.carregando = false;
      }
    });
  }
}
