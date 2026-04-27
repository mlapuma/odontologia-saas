import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AgendamentoDetalhe } from '../../../models/agendamento-detalhe.model';
import { AgendamentoRequest } from '../../../models/agendamento-request-model';
import { Paciente } from '../../../models/paciente.model';
import { Procedimento } from '../../../models/procedimento.model';
import { Profissional } from '../../../models/profissional.model';
import { AgendaService } from '../../../services/agenda.service';
import { PacienteService } from '../../../services/paciente.service';
import { ProcedimentoService } from '../../../services/procedimento.service';
import { ProfissionalService } from '../../../services/profissional.service';

@Component({
  selector: 'app-agenda-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agenda-modal.component.html',
  styleUrl: './agenda-modal.component.css'
})
export class AgendaModalComponent implements OnChanges {

  @Input() aberto = false;
  @Input() dataHoraInicio = '';
  @Input() agendamentoId?: number | null;
  @Output() fechar = new EventEmitter<void>();
  @Output() salvo = new EventEmitter<void>();

  pacientes: Paciente[] = [];
  procedimentos: Procedimento[] = [];
  profissionais: Profissional[] = [];
  detalhe?: AgendamentoDetalhe;
  carregando = false;
  salvando = false;
  erro = '';

  form = this.novoForm();

  constructor(
    private agendaService: AgendaService,
    private pacienteService: PacienteService,
    private procedimentoService: ProcedimentoService,
    private profissionalService: ProfissionalService
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aberto'] && this.aberto) {
      this.prepararModal();
    }
  }

  salvarAgendamento(): void {
    this.erro = '';

    if (!this.form.pacienteId || !this.form.profissionalId || !this.form.dataHoraInicio || !this.form.procedimentoId) {
      this.erro = 'Preencha paciente, profissional, horário e procedimento.';
      return;
    }

    const tenantId = Number(localStorage.getItem('tenantId'));
    if (!tenantId) {
      this.erro = 'Não foi possível identificar a clínica logada.';
      return;
    }

    const request: AgendamentoRequest = {
      tenantId,
      pacienteId: Number(this.form.pacienteId),
      profissionalId: Number(this.form.profissionalId),
      tabelaPrecoId: null,
      dataHoraInicio: this.normalizarDataHora(this.form.dataHoraInicio),
      observacoes: this.form.observacoes,
      procedimentos: [
        {
          procedimentoId: Number(this.form.procedimentoId),
          quantidade: Number(this.form.quantidade) || 1
        }
      ]
    };

    this.salvando = true;
    this.agendaService.criar(request).subscribe({
      next: () => {
        this.salvando = false;
        this.salvo.emit();
      },
      error: (err) => {
        this.salvando = false;
        this.erro = err?.error?.message || 'Erro ao salvar agendamento.';
      }
    });
  }

  fecharModal(): void {
    this.fechar.emit();
  }

  get modoDetalhe(): boolean {
    return !!this.agendamentoId;
  }

  get procedimentoSelecionado(): Procedimento | undefined {
    return this.procedimentos.find(item => item.id === Number(this.form.procedimentoId));
  }

  get valorEstimado(): number {
    const procedimento = this.procedimentoSelecionado;
    return procedimento ? Number(procedimento.valorBase) * (Number(this.form.quantidade) || 1) : 0;
  }

  get duracaoEstimada(): number {
    const procedimento = this.procedimentoSelecionado;
    return procedimento ? Number(procedimento.duracaoMinutos) * (Number(this.form.quantidade) || 1) : 0;
  }

  private prepararModal(): void {
    this.erro = '';
    this.detalhe = undefined;
    this.form = this.novoForm();
    this.carregando = true;

    forkJoin({
      pacientes: this.pacienteService.listar(),
      procedimentos: this.procedimentoService.listar(),
      profissionais: this.profissionalService.listar()
    }).subscribe({
      next: ({ pacientes, procedimentos, profissionais }) => {
        this.pacientes = pacientes;
        this.procedimentos = procedimentos.filter(item => item.ativo !== false);
        this.profissionais = profissionais.filter(item => item.ativo !== false);
        if (!this.form.profissionalId && this.profissionais.length > 0) {
          this.form.profissionalId = this.profissionais[0].id || null;
        }

        if (this.agendamentoId) {
          this.carregarDetalhe(this.agendamentoId);
          return;
        }

        this.form.dataHoraInicio = this.paraInputDateTime(this.dataHoraInicio);
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
        this.erro = 'Erro ao carregar dados do agendamento.';
      }
    });
  }

  private carregarDetalhe(id: number): void {
    this.agendaService.detalhar(id).subscribe({
      next: (detalhe) => {
        this.detalhe = detalhe;
        const procedimento = detalhe.procedimentos?.[0];
        this.form = {
          pacienteId: detalhe.pacienteId,
          profissionalId: detalhe.profissionalId,
          dataHoraInicio: this.paraInputDateTime(detalhe.dataHoraInicio),
          procedimentoId: procedimento?.procedimentoId || null,
          quantidade: procedimento?.quantidade || 1,
          observacoes: ''
        };
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
        this.erro = 'Erro ao carregar detalhes do agendamento.';
      }
    });
  }

  private novoForm() {
    return {
      pacienteId: null as number | null,
      profissionalId: null as number | null,
      dataHoraInicio: '',
      procedimentoId: null as number | null,
      quantidade: 1,
      observacoes: ''
    };
  }

  private paraInputDateTime(valor: string): string {
    if (!valor) {
      return '';
    }

    const data = new Date(valor);
    if (Number.isNaN(data.getTime())) {
      return valor.slice(0, 16);
    }

    const ano = data.getFullYear();
    const mes = this.doisDigitos(data.getMonth() + 1);
    const dia = this.doisDigitos(data.getDate());
    const hora = this.doisDigitos(data.getHours());
    const minuto = this.doisDigitos(data.getMinutes());
    return `${ano}-${mes}-${dia}T${hora}:${minuto}`;
  }

  private normalizarDataHora(valor: string): string {
    return valor.length === 16 ? `${valor}:00` : valor;
  }

  private doisDigitos(valor: number): string {
    return String(valor).padStart(2, '0');
  }
}
