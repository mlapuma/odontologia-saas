import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Paciente } from '../../models/paciente.model';
import { Procedimento } from '../../models/procedimento.model';
import { TratamentoRealizado } from '../../models/tratamento-realizado.model';
import { PacienteService } from '../../services/paciente.service';
import { ProcedimentoService } from '../../services/procedimento.service';
import { TratamentoRealizadoService } from '../../services/tratamento-realizado.service';

@Component({
  selector: 'app-tratamentos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tratamentos.component.html',
  styleUrl: './tratamentos.component.css'
})
export class TratamentosComponent implements OnInit {
  pacientes: Paciente[] = [];
  procedimentos: Procedimento[] = [];
  tratamentos: TratamentoRealizado[] = [];
  tratamentosOdontologicos = [
    'Avaliação odontológica',
    'Profilaxia / limpeza',
    'Aplicação de flúor',
    'Raspagem periodontal',
    'Tratamento periodontal',
    'Clareamento dental',
    'Restauração em resina',
    'Restauração em amálgama',
    'Tratamento de canal',
    'Retratamento de canal',
    'Extração dentária',
    'Extração de siso',
    'Cirurgia oral menor',
    'Implante dentário',
    'Protocolo sobre implantes',
    'Próteses fixas',
    'Prótese removível',
    'Prótese total',
    'Coroa dentária',
    'Lente de contato dental',
    'Faceta em resina',
    'Faceta em porcelana',
    'Aparelho ortodôntico',
    'Manutenção ortodôntica',
    'Alinhadores transparentes',
    'Tratamento de bruxismo',
    'Placa miorrelaxante',
    'Odontopediatria',
    'Selante dental',
    'Radiografia odontológica',
    'Enxerto ósseo',
    'Gengivoplastia',
    'Frenectomia',
    'Urgência odontológica'
  ];

  form = this.novoForm();
  carregando = false;
  salvando = false;
  mensagem = '';
  erro = '';
  pacienteIdFiltro?: number;
  editandoId?: number;
  formularioAberto = false;
  filtroPaciente = '';

  constructor(
    private pacienteService: PacienteService,
    private procedimentoService: ProcedimentoService,
    private tratamentoService: TratamentoRealizadoService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const pacienteId = Number(this.route.snapshot.queryParamMap.get('pacienteId'));
    if (pacienteId) {
      this.pacienteIdFiltro = pacienteId;
      this.form.pacienteId = pacienteId;
    }

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

    const request = {
      pacienteId: Number(this.form.pacienteId),
      procedimentoId: this.form.procedimentoId ? Number(this.form.procedimentoId) : null,
      tratamento: this.tratamentoDescricao,
      valorPago: Number(this.form.valorPago),
      dataRealizacao: this.form.dataRealizacao,
      observacoes: this.form.observacoes
    };

    this.salvando = true;
    const acao = this.editandoId
      ? this.tratamentoService.atualizar(this.editandoId, request)
      : this.tratamentoService.salvar(request);

    acao.subscribe({
      next: () => {
        this.salvando = false;
        this.mensagem = this.editandoId ? 'Tratamento atualizado com sucesso.' : 'Tratamento registrado com sucesso.';
        this.cancelarEdicao();
        this.carregarTratamentos();
      },
      error: (err) => {
        this.salvando = false;
        this.erro = err?.error?.message || 'Erro ao registrar tratamento.';
      }
    });
  }

  editarTratamento(tratamento: TratamentoRealizado): void {
    if (!tratamento.id) {
      return;
    }

    this.editandoId = tratamento.id;
    this.formularioAberto = true;
    this.mensagem = '';
    this.erro = '';
    this.form = {
      pacienteId: tratamento.pacienteId,
      procedimentoId: tratamento.procedimentoId || null,
      procedimentoSelecao: tratamento.procedimentoId ? `procedimento-${tratamento.procedimentoId}` : `padrao-${tratamento.tratamento}`,
      tratamento: tratamento.tratamento,
      valorPago: Number(tratamento.valorPago) || 0,
      dataRealizacao: tratamento.dataRealizacao,
      observacoes: tratamento.observacoes || ''
    };
  }

  excluirTratamento(tratamento: TratamentoRealizado): void {
    if (!tratamento.id) {
      return;
    }

    const confirmou = confirm(`Deseja excluir o tratamento "${tratamento.tratamento}"?`);
    if (!confirmou) {
      return;
    }

    this.mensagem = '';
    this.erro = '';
    this.tratamentoService.excluir(tratamento.id).subscribe({
      next: () => {
        this.mensagem = 'Tratamento excluido com sucesso.';
        if (this.editandoId === tratamento.id) {
          this.cancelarEdicao();
        }
        this.carregarTratamentos();
      },
      error: (err) => {
        this.erro = err?.error?.message || 'Erro ao excluir tratamento.';
      }
    });
  }

  cancelarEdicao(): void {
    this.editandoId = undefined;
    this.form = this.novoForm();
    if (this.pacienteIdFiltro) {
      this.form.pacienteId = this.pacienteIdFiltro;
    }
    this.formularioAberto = false;
  }

  novoTratamento(): void {
    this.mensagem = '';
    this.erro = '';
    this.editandoId = undefined;
    this.form = this.novoForm();
    if (this.pacienteIdFiltro) {
      this.form.pacienteId = this.pacienteIdFiltro;
    }
    this.formularioAberto = true;
  }

  nomePaciente(id: number): string {
    return this.pacientes.find(paciente => paciente.id === id)?.nome || 'Paciente';
  }

  get tratamentosFiltrados(): TratamentoRealizado[] {
    const termo = this.filtroPaciente.trim().toLowerCase();
    if (!termo) {
      return this.tratamentos;
    }

    return this.tratamentos.filter(tratamento =>
      this.nomePaciente(tratamento.pacienteId).toLowerCase().includes(termo)
    );
  }

  get tratamentoDescricao(): string {
    if (this.form.tratamento?.trim()) {
      return this.form.tratamento.trim();
    }
    return this.procedimentos.find(item => item.id === Number(this.form.procedimentoId))?.nome || '';
  }

  get tratamentosParaSelecao(): string[] {
    const nomesProcedimentos = this.procedimentos.map(item => item.nome);
    return Array.from(new Set([...this.tratamentosOdontologicos, ...nomesProcedimentos]))
      .sort((a, b) => a.localeCompare(b));
  }

  selecionarProcedimento(valor: string): void {
    this.form.procedimentoSelecao = valor;

    if (!valor) {
      this.form.procedimentoId = null;
      return;
    }

    if (valor.startsWith('procedimento-')) {
      const procedimentoId = Number(valor.replace('procedimento-', ''));
      const procedimento = this.procedimentos.find(item => item.id === procedimentoId);
      this.form.procedimentoId = procedimentoId;
      this.form.tratamento = procedimento?.nome || '';
      return;
    }

    if (valor.startsWith('padrao-')) {
      this.form.procedimentoId = null;
      this.form.tratamento = valor.replace('padrao-', '');
    }
  }

  private carregarDados(): void {
    this.carregando = true;
    forkJoin({
      pacientes: this.pacienteService.listar(),
      procedimentos: this.procedimentoService.listar(),
      tratamentos: this.tratamentoService.listar(this.pacienteIdFiltro)
    }).subscribe({
      next: ({ pacientes, procedimentos, tratamentos }) => {
        this.pacientes = pacientes;
        this.procedimentos = procedimentos.filter(item => item.ativo !== false);
        this.tratamentos = tratamentos;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar tratamentos.';
        this.carregando = false;
      }
    });
  }

  private carregarTratamentos(): void {
    this.tratamentoService.listar(this.pacienteIdFiltro).subscribe({
      next: (response) => this.tratamentos = response
    });
  }

  private novoForm() {
    return {
      pacienteId: null as number | null,
      procedimentoId: null as number | null,
      procedimentoSelecao: '',
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
