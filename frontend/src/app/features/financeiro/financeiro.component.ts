import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { FinanceiroResumo, Pagamento } from '../../models/pagamento.model';
import { Paciente } from '../../models/paciente.model';
import { PacienteService } from '../../services/paciente.service';
import { PagamentoService } from '../../services/pagamento.service';

@Component({
  selector: 'app-financeiro',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './financeiro.component.html',
  styleUrl: './financeiro.component.css'
})
export class FinanceiroComponent implements OnInit {
  resumo?: FinanceiroResumo;
  pagamentos: Pagamento[] = [];
  pendentes: Pagamento[] = [];
  pacientes: Paciente[] = [];
  carregando = false;
  erro = '';

  constructor(
    private pagamentoService: PagamentoService,
    private pacienteService: PacienteService
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = '';

    forkJoin({
      resumo: this.pagamentoService.resumo(),
      pagamentos: this.pagamentoService.listar(),
      pendentes: this.pagamentoService.listarPendentes(),
      pacientes: this.pacienteService.listar()
    }).subscribe({
      next: ({ resumo, pagamentos, pendentes, pacientes }) => {
        this.resumo = resumo;
        this.pagamentos = pagamentos;
        this.pendentes = pendentes;
        this.pacientes = pacientes;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar financeiro.';
        this.carregando = false;
      }
    });
  }

  nomePaciente(id: number): string {
    return this.pacientes.find(paciente => paciente.id === id)?.nome || 'Paciente';
  }
}
