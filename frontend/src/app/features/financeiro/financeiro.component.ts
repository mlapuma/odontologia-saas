import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Paciente } from '../../models/paciente.model';
import { Procedimento } from '../../models/procedimento.model';
import { PacienteReativacao, TratamentoRealizado } from '../../models/tratamento-realizado.model';
import { PacienteService } from '../../services/paciente.service';
import { ProcedimentoService } from '../../services/procedimento.service';
import { TratamentoRealizadoService } from '../../services/tratamento-realizado.service';

@Component({
  selector: 'app-financeiro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './financeiro.component.html',
  styleUrl: './financeiro.component.css'
})
export class FinanceiroComponent implements OnInit {

  pacientes: Paciente[] = [];
  procedimentos: Procedimento[] = [];
  tratamentos: TratamentoRealizado[] = [];
  pacientesReativacao: PacienteReativacao[] = [];

  form = this.novoForm();
  diasSemComparecer = 180;
  carregando = false;
  salvando = false;
  mensagem = '';
  erro = '';

  constructor(
    private pacienteService: PacienteService,
    private procedimentoService: ProcedimentoService,
    private tratamentoService: TratamentoRealizadoService
  ) { }

  ngOnInit(): void {
    this.carregarDados();
  }

  salvarTratamento(): void {
    this.mensagem = '';
    this.erro = '';

    if (!this.form.pacienteId) {
      this.erro = 'Selecione um paciente.';
      return;
    }
    if (!this.form.tratamento && !this.form.procedimentoId) {
      this.erro = 'Informe o tratamento realizado.';
      return;
    }
    if (this.form.valorPago === null || Number(this.form.valorPago) < 0) {
      this.erro = 'Informe o valor pago.';
      return;
    }
    if (!this.form.dataRealizacao) {
      this.erro = 'Informe a data do tratamento.';
      return;
    }

    this.salvando = true;
    this.tratamentoService.salvar({
      pacienteId: Number(this.form.pacienteId),
      procedimentoId: this.form.procedimentoId ? Number(this.form.procedimentoId) : null,
      tratamento: this.tratamentoDescricao,
      valorPago: Number(this.form.valorPago),
      dataRealizacao: this.form.dataRealizacao,
      observacoes: this.form.observacoes
    }).subscribe({
      next: () => {
        this.salvando = false;
        this.mensagem = 'Tratamento registrado com sucesso.';
        this.form = this.novoForm();
        this.carregarTratamentos();
        this.carregarReativacao();
      },
      error: (err) => {
        this.salvando = false;
        this.erro = err?.error?.message || 'Erro ao registrar tratamento.';
      }
    });
  }

  carregarReativacao(): void {
    this.tratamentoService.pacientesParaReativacao(Number(this.diasSemComparecer) || 180).subscribe({
      next: (response) => this.pacientesReativacao = response,
      error: () => this.erro = 'Erro ao carregar pacientes para reativacao.'
    });
  }

  nomePaciente(id: number): string {
    return this.pacientes.find(paciente => paciente.id === id)?.nome || 'Paciente';
  }

  get tratamentoDescricao(): string {
    if (this.form.tratamento?.trim()) {
      return this.form.tratamento.trim();
    }
    return this.procedimentos.find(item => item.id === Number(this.form.procedimentoId))?.nome || '';
  }

  private carregarDados(): void {
    this.carregando = true;
    forkJoin({
      pacientes: this.pacienteService.listar(),
      procedimentos: this.procedimentoService.listar(),
      tratamentos: this.tratamentoService.listar(),
      reativacao: this.tratamentoService.pacientesParaReativacao(this.diasSemComparecer)
    }).subscribe({
      next: ({ pacientes, procedimentos, tratamentos, reativacao }) => {
        this.pacientes = pacientes;
        this.procedimentos = procedimentos.filter(item => item.ativo !== false);
        this.tratamentos = tratamentos;
        this.pacientesReativacao = reativacao;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar dados financeiros.';
        this.carregando = false;
      }
    });
  }

  private carregarTratamentos(): void {
    this.tratamentoService.listar().subscribe({
      next: (response) => this.tratamentos = response
    });
  }

  private novoForm() {
    return {
      pacienteId: null as number | null,
      procedimentoId: null as number | null,
      tratamento: '',
      valorPago: 0,
      dataRealizacao: this.hoje(),
      observacoes: ''
    };
  }

  private hoje(): string {
    const data = new Date();
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }
}
