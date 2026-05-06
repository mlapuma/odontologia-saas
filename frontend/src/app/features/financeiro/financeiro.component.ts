import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { FinanceiroResumo, Pagamento } from '../../models/pagamento.model';
import { Paciente } from '../../models/paciente.model';
import { TratamentoRealizado } from '../../models/tratamento-realizado.model';
import { PacienteService } from '../../services/paciente.service';
import { PagamentoService } from '../../services/pagamento.service';
import { TratamentoRealizadoService } from '../../services/tratamento-realizado.service';

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
  tratamentos: TratamentoRealizado[] = [];
  pacientes: Paciente[] = [];
  carregando = false;
  erro = '';
  anosAbertos = new Set<string>();
  anosHistoricoAbertos = new Set<string>();
  paginaHistoricoPorAno: Record<string, number> = {};
  exibirRecebidoPorAno = false;
  exibirHistoricoFinanceiro = false;
  readonly itensPorPaginaHistorico = 8;

  constructor(
    private pagamentoService: PagamentoService,
    private pacienteService: PacienteService,
    private tratamentoService: TratamentoRealizadoService
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
      tratamentos: this.tratamentoService.listar(),
      pacientes: this.pacienteService.listar()
    }).subscribe({
      next: ({ resumo, pagamentos, pendentes, tratamentos, pacientes }) => {
        this.resumo = resumo;
        this.pagamentos = pagamentos;
        this.pendentes = pendentes;
        this.tratamentos = tratamentos;
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

  get lancamentosFinanceiros(): LancamentoFinanceiro[] {
    const pagamentos = this.pagamentos.map(pagamento => ({
      id: `pagamento-${pagamento.id}`,
      pacienteId: pagamento.pacienteId,
      valorTotal: Number(pagamento.valorTotal || 0),
      valorPago: Number(pagamento.valorPago || 0),
      formaPagamento: pagamento.formaPagamento || null,
      status: pagamento.status,
      dataPagamento: pagamento.dataPagamento || null,
      observacoes: pagamento.observacoes || '',
      origem: 'Pagamento',
      descricao: 'Pagamento registrado'
    }));

    const tratamentos = this.tratamentos
      .filter(tratamento => Number(tratamento.valorPago || 0) > 0)
      .map(tratamento => ({
        id: `tratamento-${tratamento.id}`,
        pacienteId: tratamento.pacienteId,
        valorTotal: Number(tratamento.valorTotal || 0),
        valorPago: Number(tratamento.valorPago || 0),
        formaPagamento: tratamento.formaPagamento || null,
        status: this.statusTratamento(tratamento),
        dataPagamento: tratamento.dataRealizacao ? `${tratamento.dataRealizacao}T00:00:00` : null,
        observacoes: tratamento.observacoes || '',
        origem: 'Tratamento',
        descricao: tratamento.tratamento || 'Tratamento'
      }));

    return [...pagamentos, ...tratamentos]
      .sort((a, b) => this.timestamp(b.dataPagamento) - this.timestamp(a.dataPagamento));
  }

  get recebimentosMensais(): RecebimentoMensal[] {
    const agrupado = new Map<string, RecebimentoMensal>();

    this.lancamentosFinanceiros
      .filter(lancamento => Number(lancamento.valorPago || 0) > 0)
      .forEach(lancamento => {
        const data = lancamento.dataPagamento ? new Date(lancamento.dataPagamento) : null;
        if (!data || Number.isNaN(data.getTime())) {
          return;
        }

        const chave = `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`;
        const atual = agrupado.get(chave) || {
          chave,
          mes: this.nomeMes(data),
          totalRecebido: 0,
          quantidade: 0
        };
        atual.totalRecebido += Number(lancamento.valorPago || 0);
        atual.quantidade += 1;
        agrupado.set(chave, atual);
      });

    return Array.from(agrupado.values())
      .sort((a, b) => b.chave.localeCompare(a.chave));
  }

  get recebimentosPorAno(): RecebimentoAnual[] {
    const agrupado = new Map<string, RecebimentoAnual>();

    this.recebimentosMensais.forEach(mes => {
      const ano = mes.chave.substring(0, 4);
      const atual = agrupado.get(ano) || {
        ano,
        totalRecebido: 0,
        quantidade: 0,
        meses: []
      };
      atual.totalRecebido += mes.totalRecebido;
      atual.quantidade += mes.quantidade;
      atual.meses.push(mes);
      agrupado.set(ano, atual);
    });

    return Array.from(agrupado.values())
      .map(ano => ({
        ...ano,
        meses: ano.meses.sort((a, b) => b.chave.localeCompare(a.chave))
      }))
      .sort((a, b) => b.ano.localeCompare(a.ano));
  }

  get historicoPorAno(): HistoricoAnual[] {
    const agrupado = new Map<string, HistoricoAnual>();

    this.lancamentosFinanceiros.forEach(lancamento => {
      const data = lancamento.dataPagamento ? new Date(lancamento.dataPagamento) : null;
      const ano = data && !Number.isNaN(data.getTime()) ? String(data.getFullYear()) : 'Sem data';
      const atual = agrupado.get(ano) || {
        ano,
        totalRecebido: 0,
        quantidade: 0,
        lancamentos: []
      };
      atual.totalRecebido += Number(lancamento.valorPago || 0);
      atual.quantidade += 1;
      atual.lancamentos.push(lancamento);
      agrupado.set(ano, atual);
    });

    return Array.from(agrupado.values())
      .map(ano => ({
        ...ano,
        lancamentos: ano.lancamentos.sort((a, b) => this.timestamp(b.dataPagamento) - this.timestamp(a.dataPagamento))
      }))
      .sort((a, b) => {
        if (a.ano === 'Sem data') {
          return 1;
        }
        if (b.ano === 'Sem data') {
          return -1;
        }
        return b.ano.localeCompare(a.ano);
      });
  }

  alternarAno(ano: string): void {
    if (this.anosAbertos.has(ano)) {
      this.anosAbertos.delete(ano);
      return;
    }
    this.anosAbertos.add(ano);
  }

  anoAberto(ano: string): boolean {
    return this.anosAbertos.has(ano);
  }

  alternarRecebidoPorAno(): void {
    this.exibirRecebidoPorAno = !this.exibirRecebidoPorAno;
  }

  alternarHistoricoFinanceiro(): void {
    this.exibirHistoricoFinanceiro = !this.exibirHistoricoFinanceiro;
  }

  alternarAnoHistorico(ano: string): void {
    if (this.anosHistoricoAbertos.has(ano)) {
      this.anosHistoricoAbertos.delete(ano);
      return;
    }
    this.anosHistoricoAbertos.add(ano);
    this.paginaHistoricoPorAno[ano] = this.paginaHistoricoPorAno[ano] || 1;
  }

  anoHistoricoAberto(ano: string): boolean {
    return this.anosHistoricoAbertos.has(ano);
  }

  paginaHistorico(ano: string): number {
    return this.paginaHistoricoPorAno[ano] || 1;
  }

  totalPaginasHistorico(ano: HistoricoAnual): number {
    return Math.max(Math.ceil(ano.lancamentos.length / this.itensPorPaginaHistorico), 1);
  }

  historicoPaginado(ano: HistoricoAnual): LancamentoFinanceiro[] {
    const pagina = this.paginaHistorico(ano.ano);
    const inicio = (pagina - 1) * this.itensPorPaginaHistorico;
    return ano.lancamentos.slice(inicio, inicio + this.itensPorPaginaHistorico);
  }

  alterarPaginaHistorico(ano: HistoricoAnual, direcao: number): void {
    const paginaAtual = this.paginaHistorico(ano.ano);
    const proximaPagina = Math.min(Math.max(paginaAtual + direcao, 1), this.totalPaginasHistorico(ano));
    this.paginaHistoricoPorAno[ano.ano] = proximaPagina;
  }

  labelFormaPagamento(valor?: string | null): string {
    const labels: Record<string, string> = {
      PIX: 'Pix',
      CARTAO_CREDITO_PARCELADO: 'Cartao credito parcelado',
      CARTAO_CREDITO_AVISTA: 'Cartao credito a vista',
      CARTAO_DEBITO: 'Cartao debito',
      DINHEIRO: 'Dinheiro'
    };
    return valor ? labels[valor] || valor : '-';
  }

  private statusTratamento(tratamento: TratamentoRealizado): string {
    if (Boolean(tratamento.finalizado)) {
      return 'PAGO';
    }
    if (Number(tratamento.saldo || 0) > 0) {
      return 'PARCIAL';
    }
    return 'PAGO';
  }

  private timestamp(data?: string | null): number {
    if (!data) {
      return 0;
    }
    const parsed = new Date(data);
    return Number.isNaN(parsed.getTime()) ? 0 : parsed.getTime();
  }

  private nomeMes(data: Date): string {
    return new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(data);
  }
}

interface LancamentoFinanceiro {
  id: string;
  pacienteId: number;
  valorTotal: number;
  valorPago: number;
  formaPagamento?: string | null;
  status: string;
  dataPagamento?: string | null;
  observacoes?: string;
  origem: string;
  descricao: string;
}

interface RecebimentoMensal {
  chave: string;
  mes: string;
  totalRecebido: number;
  quantidade: number;
}

interface RecebimentoAnual {
  ano: string;
  totalRecebido: number;
  quantidade: number;
  meses: RecebimentoMensal[];
}

interface HistoricoAnual {
  ano: string;
  totalRecebido: number;
  quantidade: number;
  lancamentos: LancamentoFinanceiro[];
}
