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
  pacienteHistoricoId: number | null = null;
  tratamentoSelecionadoId?: number;

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
      this.pacienteHistoricoId = pacienteId;
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
    const saldoAberto = this.saldoAbertoAvaliacaoFormulario();
    if (saldoAberto !== null && Number(this.form.valorPago) > saldoAberto) {
      this.erro = 'Valor pago nao pode ser maior que o saldo em aberto da avaliacao.';
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
        this.erro = this.mensagemErro(err, 'Erro ao registrar tratamento.');
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
        this.erro = this.mensagemErro(err, 'Erro ao excluir tratamento.');
      }
    });
  }

  finalizarTratamento(): void {
    const tratamentoBase = this.tratamentoSelecionado;
    if (!tratamentoBase?.id || tratamentoBase.finalizado) {
      return;
    }
    if (!this.podeFinalizarTratamento) {
      this.erro = 'O tratamento so pode ser finalizado quando o saldo estiver zerado.';
      return;
    }

    const confirmou = confirm(`Finalizar o tratamento de ${this.nomePaciente(tratamentoBase.pacienteId)} e liberar uma nova avaliacao para este paciente?`);
    if (!confirmou) {
      return;
    }

    this.mensagem = '';
    this.erro = '';
    this.tratamentoService.finalizar(tratamentoBase.id).subscribe({
      next: () => {
        this.mensagem = 'Tratamento finalizado com sucesso. Uma nova avaliacao podera ser iniciada para o paciente.';
        if (this.editandoId === tratamentoBase.id) {
          this.cancelarEdicao();
        }
        this.carregarTratamentos();
      },
      error: (err) => {
        this.erro = this.mensagemErro(err, 'Erro ao finalizar tratamento.');
      }
    });
  }

  cancelarEdicao(): void {
    this.editandoId = undefined;
    this.form = this.novoForm();
    const pacienteIdAtual = this.pacienteAtualId();
    if (pacienteIdAtual) {
      this.form.pacienteId = pacienteIdAtual;
    }
    this.formularioAberto = false;
  }

  novoTratamento(): void {
    this.mensagem = '';
    this.erro = '';
    this.editandoId = undefined;
    this.form = this.novoForm();
    const pacienteIdAtual = this.pacienteAtualId();
    if (pacienteIdAtual) {
      this.form.pacienteId = pacienteIdAtual;
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

  labelFormaPagamento(valor?: string | null): string {
    if (!valor) {
      return '-';
    }
    return this.formasPagamento.find(forma => forma.valor === valor)?.label || valor;
  }

  get tratamentosFiltrados(): TratamentoRealizado[] {
    if (!this.possuiFiltroPaciente) {
      return [];
    }

    return this.tratamentosAtivosPaciente.length > 0
      ? this.tratamentosAtivosPaciente
      : this.tratamentosFinalizadosPaciente;
  }

  get possuiFiltroPaciente(): boolean {
    return Boolean(this.pacienteHistoricoId);
  }

  get tratamentosPacienteSelecionado(): TratamentoRealizado[] {
    if (!this.pacienteHistoricoId) {
      return [];
    }

    return this.tratamentos.filter(tratamento => tratamento.pacienteId === Number(this.pacienteHistoricoId));
  }

  get tratamentosAtivosPaciente(): TratamentoRealizado[] {
    return this.tratamentosPacienteSelecionado.filter(tratamento => !tratamento.finalizado);
  }

  get tratamentosFinalizadosPaciente(): TratamentoRealizado[] {
    const finalizados = this.tratamentosPacienteSelecionado.filter(tratamento => tratamento.finalizado);
    const ultimaFinalizacao = finalizados
      .map(tratamento => this.chaveFinalizacao(tratamento))
      .sort()
      .pop();

    if (!ultimaFinalizacao) {
      return [];
    }

    return finalizados.filter(tratamento => this.chaveFinalizacao(tratamento) === ultimaFinalizacao);
  }

  get possuiTratamentoAtivo(): boolean {
    return this.tratamentosAtivosPaciente.length > 0;
  }

  get tratamentoSelecionado(): TratamentoRealizado | undefined {
    if (this.tratamentoSelecionadoId) {
      return this.tratamentosFiltrados.find(tratamento => tratamento.id === this.tratamentoSelecionadoId)
        || this.tratamentosFiltrados[0];
    }
    return this.tratamentosFiltrados[0];
  }

  get podeFinalizarTratamento(): boolean {
    return Boolean(this.possuiTratamentoAtivo && this.tratamentoSelecionado && !this.tratamentoSelecionado.finalizado && this.valorRestanteTratamentos <= 0);
  }

  selecionarTratamento(tratamento: TratamentoRealizado): void {
    this.tratamentoSelecionadoId = tratamento.id;
    this.mensagem = '';
    this.erro = '';
  }

  aoSelecionarPacienteHistorico(): void {
    this.tratamentoSelecionadoId = undefined;
    this.filtroPaciente = '';
    this.mensagem = '';
    this.erro = '';
    if (!this.editandoId) {
      this.form.pacienteId = this.pacienteHistoricoId;
      this.aplicarAvaliacaoExistenteDoPaciente();
    }
  }

  get tratamentoDescricao(): string {
    if (this.form.tratamento?.trim()) {
      return this.form.tratamento.trim();
    }
    return this.procedimentos.find(item => item.id === Number(this.form.procedimentoId))?.nome || '';
  }

  get tratamentosParaSelecao(): string[] {
    const nomesProcedimentos = this.procedimentos.map(item => item.nome);
    return Array.from(new Set(nomesProcedimentos))
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
      this.aplicarValorTotalMinimo();
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
      this.aplicarValorTotalMinimo();
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

  aoAlterarValorTratamento(): void {
    this.aplicarValorTotalMinimo();
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

    const totalRegistrado = this.tratamentosAbertosDaAvaliacaoAtual(this.form.pacienteId)
      .filter(tratamento => tratamento.id !== this.editandoId)
      .reduce((total, tratamento) => total + Number(tratamento.valorTratamento || tratamento.valorTotal || tratamento.valorPago || 0), 0);

    return totalRegistrado + Number(this.form.valorTratamento || 0);
  }

  get valorPagoPacienteFormulario(): number {
    if (!this.form.pacienteId) {
      return Number(this.form.valorPago || 0);
    }

    const totalPagoRegistrado = this.tratamentosAbertosDaAvaliacaoAtual(this.form.pacienteId)
      .filter(tratamento => tratamento.id !== this.editandoId)
      .reduce((total, tratamento) => total + Number(tratamento.valorPago || 0), 0);

    return totalPagoRegistrado + Number(this.form.valorPago || 0);
  }

  get valorRestanteAvaliacaoFormulario(): number {
    const saldoAberto = this.saldoAbertoAvaliacaoFormulario();
    if (saldoAberto !== null) {
      return Math.max(saldoAberto - Number(this.form.valorPago || 0), 0);
    }
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
    if (this.possuiTratamentoAtivo) {
      return this.saldoCalculadoTratamentos(this.tratamentosAtivosPaciente);
    }
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
        this.ajustarTratamentoSelecionado();
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
      next: (response) => {
        this.tratamentos = response;
        this.ajustarTratamentoSelecionado();
      }
    });
  }

  private ajustarTratamentoSelecionado(): void {
    if (!this.tratamentoSelecionadoId) {
      return;
    }
    const selecionadoExiste = this.tratamentosFiltrados.some(tratamento => tratamento.id === this.tratamentoSelecionadoId);
    if (!selecionadoExiste) {
      this.tratamentoSelecionadoId = undefined;
    }
  }

  private pacienteAtualId(): number | null {
    return this.pacienteIdFiltro
      || this.pacienteHistoricoId
      || this.tratamentoSelecionado?.pacienteId
      || null;
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
    this.form.valorTotal = valorTotalAvaliacao;
    this.aplicarValorTotalMinimo();
  }

  private aplicarValorTotalMinimo(): void {
    const valorTratamento = Number(this.form.valorTratamento || 0);
    const valorTotal = Number(this.form.valorTotal || 0);

    if (valorTratamento > 0 && valorTotal <= 0) {
      this.form.valorTotal = valorTratamento;
    }
  }

  private valorTotalAvaliacaoPaciente(pacienteId?: number | null): number {
    if (!pacienteId) {
      return 0;
    }

    return this.tratamentosAbertosDaAvaliacaoAtual(pacienteId)
      .filter(tratamento => tratamento.id !== this.editandoId)
      .reduce((maior, tratamento) => Math.max(maior, Number(tratamento.valorTotal || 0)), 0);
  }

  private tratamentosAbertosDaAvaliacaoAtual(pacienteId?: number | null): TratamentoRealizado[] {
    if (!pacienteId) {
      return [];
    }

    const tratamentosPaciente = this.tratamentos.filter(tratamento => tratamento.pacienteId === Number(pacienteId));
    return tratamentosPaciente
      .filter(tratamento => !tratamento.finalizado);
  }

  private chaveFinalizacao(tratamento: TratamentoRealizado): string {
    return tratamento.dataFinalizacao || tratamento.dataRealizacao || '';
  }

  private saldoAbertoAvaliacaoFormulario(): number | null {
    if (!this.form.pacienteId) {
      return null;
    }

    const tratamentosAbertos = this.tratamentosAbertosDaAvaliacaoAtual(this.form.pacienteId)
      .filter(tratamento => tratamento.id !== this.editandoId);

    const valorTotalAvaliacao = tratamentosAbertos.reduce(
      (maior, tratamento) => Math.max(maior, Number(tratamento.valorTotal || 0)),
      Number(this.form.valorTotal || 0)
    );

    if (valorTotalAvaliacao <= 0 && tratamentosAbertos.length === 0) {
      return null;
    }

    const totalPagoOutros = tratamentosAbertos
      .reduce((total, tratamento) => total + Number(tratamento.valorPago || 0), 0);

    return Math.max(valorTotalAvaliacao - totalPagoOutros, 0);
  }

  private saldoCalculadoTratamentos(tratamentos: TratamentoRealizado[]): number {
    const valorTotalAvaliacao = tratamentos.reduce(
      (maior, tratamento) => Math.max(maior, Number(tratamento.valorTotal || 0)),
      0
    );
    const totalPago = tratamentos.reduce((total, tratamento) => total + Number(tratamento.valorPago || 0), 0);
    return Math.max(valorTotalAvaliacao - totalPago, 0);
  }

  private mensagemErro(err: any, padrao: string): string {
    if (typeof err?.error === 'string' && err.error.trim()) {
      return err.error;
    }
    return err?.error?.message || padrao;
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
