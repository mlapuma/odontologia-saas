import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardResumo } from '../../models/dashboard-resumo.model';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-google-performance',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './google-performance.component.html',
  styleUrl: './google-performance.component.css'
})
export class GooglePerformanceComponent implements OnInit {
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
        this.erro = 'Erro ao carregar desempenho no Google.';
        this.carregando = false;
      }
    });
  }
}
