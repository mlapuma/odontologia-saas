import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardResumo } from '../../models/dashboard-resumo.model';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-reativacao',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './reativacao.component.html',
  styleUrl: './reativacao.component.css'
})
export class ReativacaoComponent implements OnInit {
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
        this.erro = 'Erro ao carregar pacientes para reativar.';
        this.carregando = false;
      }
    });
  }
}
