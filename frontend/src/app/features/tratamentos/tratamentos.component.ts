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
    'Remocao de carie + curativo',
    'Exo',
    'Medicacao',
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
  dentesArcada = [
    { grupo: 'Superior direito', dentes: ['18', '17', '16', '15', '14', '13', '12', '11'] },
    { grupo: 'Superior esquerdo', dentes: ['21', '22', '23', '24', '25', '26', '27', '28'] },
    { grupo: 'Inferior esquerdo', dentes: ['38', '37', '36', '35', '34', '33', '32', '31'] },
    { grupo: 'Inferior direito', dentes: ['41', '42', '43', '44', '45', '46', '47', '48'] }
  ];
  formasPagamento = [
    { valor: 'PIX', label: 'Pix' },
    { valor: 'CARTAO_CREDITO_A_VISTA', label: 'Cartao credito a vista' },
    { valor: 'CARTAO_CREDITO_PARCELADO', label: 'Cartao credito parcelado' },
    { valor: 'CARTAO_DEBITO', label: 'Cartao debito' },
    { valor: 'DINHEIRO', label: 'Dinheiro' }
  ];

  form = this.novoForm();
  carregando = false;
  salvando = false;
  mensagem = '';
  erro = '';
  pacienteIdFiltro?: number;
  editandoId?: number;
  formularioAberto = false;
  historicoAberto = false;
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
    if (Number(this.form.valorTratamento) < 0) {
      this.erro = 'Informe o valor do tratamento.';
      return;
    }
    if (Number(this.form.valorTotal) < 0) {
      this.erro = 'Informe o valor total da avaliacao.';
      return;
    }
    if (this.valorProcedimentosPacienteFormulario > Number(this.form.valorTotal)) {
      this.erro = 'A soma dos procedimentos nao pode ser maior que o valor total da avaliacao.';
      return;
    }
    if (this.valorPagoPacienteFormulario > Number(this.form.valorTotal)) {
      this.erro = 'A soma dos pagamentos nao pode ser maior que o valor total da avaliacao.';
      return;
    }
    if (Number(this.form.valorPago) > 0 && !this.form.formaPagamento) {
      this.erro = 'Informe a forma de pagamento.';
      return;
    }
    if (this.form.formaPagamento === 'CARTAO_CREDITO_PARCELADO' && Number(this.form.parcelas) < 2) {
      this.erro = 'Informe a quantidade de parcelas.';
      return;
    }

    const request = {
      pacienteId: Number(this.form.pacienteId),
      procedimentoId: this.form.procedimentoId ? Number(this.form.procedimentoId) : null,
      tratamento: this.tratamentoDescricao,
      dente: this.form.dente || null,
      valorTratamento: Number(this.form.valorTratamento),
      valorTotal: Number(this.form.valorTotal),
      valorPago: Number(this.form.valorPago),
      formaPagamento: this.form.formaPagamento || null,
      parcelas: this.form.formaPagamento === 'CARTAO_CREDITO_PARCELADO' ? Number(this.form.parcelas) : null,
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
      procedimentoBusca: tratamento.tratamento,
      tratamento: tratamento.tratamento,
      dente: tratamento.dente || '',
      valorTratamento: Number(tratamento.valorTratamento) || Number(tratamento.valorTotal) || Number(tratamento.valorPago) || 0,
      valorTotal: Number(tratamento.valorTotal) || 0,
      valorPago: Number(tratamento.valorPago) || 0,
      formaPagamento: tratamento.formaPagamento || '',
      parcelas: tratamento.parcelas || 2,
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

  finalizarTratamento(tratamento: TratamentoRealizado): void {
    if (!tratamento.id || tratamento.finalizado) {
      return;
    }

    const confirmou = confirm(`Finalizar o tratamento "${tratamento.tratamento}" e liberar uma nova avaliacao para este paciente?`);
    if (!confirmou) {
      return;
    }

    this.mensagem = '';
    this.erro = '';
    this.tratamentoService.finalizar(tratamento.id).subscribe({
      next: () => {
        this.mensagem = 'Tratamento finalizado com sucesso. Uma nova avaliacao podera ser iniciada para o paciente.';
        if (this.editandoId === tratamento.id) {
          this.cancelarEdicao();
        }
        this.carregarTratamentos();
      },
      error: (err) => {
        this.erro = err?.error?.message || 'Erro ao finalizar tratamento.';
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
      this.aplicarAvaliacaoExistenteDoPaciente();
    }
    this.formularioAberto = true;
  }

  alternarHistorico(): void {
    this.historicoAberto = !this.historicoAberto;
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
      this.form.valorTratamento = procedimento?.valorBase || 0;
      return;
    }

    if (valor.startsWith('padrao-')) {
      this.form.procedimentoId = null;
      this.form.tratamento = valor.replace('padrao-', '');
    }
  }

  selecionarProcedimentoPorTexto(valor: string): void {
    this.form.procedimentoBusca = valor;
    this.form.procedimentoSelecao = '';
    this.form.tratamento = valor.trim();

    const termo = this.normalizarTermo(valor);
    if (!termo) {
      this.form.procedimentoId = null;
      this.form.tratamento = '';
      return;
    }

    const procedimentoCadastrado = this.procedimentos.find(item =>
      this.normalizarTermo(item.nome) === termo
    );

    if (procedimentoCadastrado) {
      this.form.procedimentoId = procedimentoCadastrado.id || null;
      this.form.procedimentoSelecao = `procedimento-${procedimentoCadastrado.id}`;
      this.form.tratamento = procedimentoCadastrado.nome;
      this.form.valorTratamento = procedimentoCadastrado.valorBase || 0;
      return;
    }

    const procedimentoPadrao = this.tratamentosOdontologicos.find(item =>
      this.normalizarTermo(item) === termo
    );

    if (procedimentoPadrao) {
      this.form.procedimentoId = null;
      this.form.procedimentoSelecao = `padrao-${procedimentoPadrao}`;
      this.form.tratamento = procedimentoPadrao;
      return;
    }

    this.form.procedimentoId = null;
  }

  limparProcedimento(): void {
    this.form.procedimentoBusca = '';
    this.form.procedimentoSelecao = '';
    this.form.procedimentoId = null;
    this.form.tratamento = '';
    this.form.valorTratamento = 0;
  }

  aoAlterarFormaPagamento(): void {
    if (this.form.formaPagamento !== 'CARTAO_CREDITO_PARCELADO') {
      this.form.parcelas = 2;
    }
  }

  aoSelecionarPaciente(): void {
    if (!this.editandoId) {
      this.aplicarAvaliacaoExistenteDoPaciente();
    }
  }

  get saldoTratamento(): number {
    return this.valorRestanteAvaliacaoFormulario;
  }

  get valorProcedimentosPacienteFormulario(): number {
    if (!this.form.pacienteId) {
      return Number(this.form.valorTratamento || 0);
    }

    const totalRegistrado = this.tratamentos
      .filter(tratamento => tratamento.pacienteId === Number(this.form.pacienteId) && tratamento.id !== this.editandoId)
      .filter(tratamento => !tratamento.finalizado)
      .reduce((total, tratamento) => total + Number(tratamento.valorTratamento || tratamento.valorTotal || tratamento.valorPago || 0), 0);

    return totalRegistrado + Number(this.form.valorTratamento || 0);
  }

  get valorPagoPacienteFormulario(): number {
    if (!this.form.pacienteId) {
      return Number(this.form.valorPago || 0);
    }

    const totalPagoRegistrado = this.tratamentos
      .filter(tratamento => tratamento.pacienteId === Number(this.form.pacienteId) && tratamento.id !== this.editandoId)
      .filter(tratamento => !tratamento.finalizado)
      .reduce((total, tratamento) => total + Number(tratamento.valorPago || 0), 0);

    return totalPagoRegistrado + Number(this.form.valorPago || 0);
  }

  get valorRestanteAvaliacaoFormulario(): number {
    return Math.max(Number(this.form.valorTotal || 0) - this.valorPagoPacienteFormulario, 0);
  }

  get diferencaAvaliacaoFormulario(): number {
    return Number(this.form.valorTotal || 0) - this.valorProcedimentosPacienteFormulario;
  }

  get valorTotalTratamentos(): number {
    return this.tratamentosFiltrados.reduce((total, tratamento) => total + Number(tratamento.valorTratamento || tratamento.valorTotal || tratamento.valorPago || 0), 0);
  }

  get valorTotalAvaliacaoTratamentos(): number {
    return this.tratamentosFiltrados.reduce((maior, tratamento) => Math.max(maior, Number(tratamento.valorTotal || 0)), 0);
  }

  get valorPagoTratamentos(): number {
    return this.tratamentosFiltrados.reduce((total, tratamento) => total + Number(tratamento.valorPago || 0), 0);
  }

  get valorRestanteTratamentos(): number {
    return Math.max(this.valorTotalAvaliacaoTratamentos - this.valorPagoTratamentos, 0);
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
        this.aplicarAvaliacaoExistenteDoPaciente();
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
      procedimentoBusca: '',
      tratamento: '',
      dente: '',
      valorTratamento: 0,
      valorTotal: 0,
      valorPago: 0,
      formaPagamento: '',
      parcelas: 2,
      dataRealizacao: this.hoje(),
      observacoes: ''
    };
  }

  private aplicarAvaliacaoExistenteDoPaciente(): void {
    const valorTotalAvaliacao = this.valorTotalAvaliacaoPaciente(this.form.pacienteId);
    if (valorTotalAvaliacao > 0) {
      this.form.valorTotal = valorTotalAvaliacao;
    }
  }

  private valorTotalAvaliacaoPaciente(pacienteId?: number | null): number {
    if (!pacienteId) {
      return 0;
    }

    return this.tratamentos
      .filter(tratamento => tratamento.pacienteId === Number(pacienteId) && tratamento.id !== this.editandoId)
      .filter(tratamento => !tratamento.finalizado)
      .reduce((maior, tratamento) => Math.max(maior, Number(tratamento.valorTotal || 0)), 0);
  }

  private hoje(): string {
    const data = new Date();
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  private normalizarTermo(valor?: string | null): string {
    return (valor || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
