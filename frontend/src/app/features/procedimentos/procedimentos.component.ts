import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Procedimento } from '../../models/procedimento.model';
import { ProcedimentoService } from '../../services/procedimento.service';

@Component({
  selector: 'app-procedimentos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './procedimentos.component.html',
  styleUrl: './procedimentos.component.css'
})
export class ProcedimentosComponent implements OnInit {
  procedimentos: Procedimento[] = [];
  valores: Record<number, number> = {};
  filtro = '';
  carregando = false;
  salvandoId?: number;
  formularioAberto = false;
  editandoId?: number;
  form = this.novoForm();
  mensagem = '';
  erro = '';

  constructor(private procedimentoService: ProcedimentoService) {}

  ngOnInit(): void {
    this.carregarProcedimentos();
  }

  get procedimentosFiltrados(): Procedimento[] {
    const termo = this.filtro.trim().toLowerCase();
    const lista = termo
      ? this.procedimentos.filter(procedimento => procedimento.nome.toLowerCase().includes(termo))
      : this.procedimentos;

    return [...lista].sort((a, b) => a.nome.localeCompare(b.nome));
  }

  salvarValor(procedimento: Procedimento): void {
    if (!procedimento.id) {
      return;
    }

    const valorBase = Number(this.valores[procedimento.id] || 0);
    if (valorBase < 0) {
      this.erro = 'Informe um valor maior ou igual a zero.';
      return;
    }

    this.mensagem = '';
    this.erro = '';
    this.salvandoId = procedimento.id;
    this.procedimentoService.atualizarValor(procedimento.id, valorBase).subscribe({
      next: (atualizado) => {
        this.salvandoId = undefined;
        this.mensagem = `Valor de ${atualizado.nome} atualizado com sucesso.`;
        this.procedimentos = this.procedimentos.map(item => item.id === atualizado.id ? atualizado : item);
        this.valores[atualizado.id] = Number(atualizado.valorBase || 0);
      },
      error: (err) => {
        this.salvandoId = undefined;
        this.erro = this.mensagemErro(err, 'Erro ao atualizar valor do procedimento.');
      }
    });
  }

  novoProcedimento(): void {
    this.form = this.novoForm();
    this.editandoId = undefined;
    this.formularioAberto = true;
    this.mensagem = '';
    this.erro = '';
  }

  editarProcedimento(procedimento: Procedimento): void {
    this.form = {
      nome: procedimento.nome,
      valorBase: Number(procedimento.valorBase || 0),
      categoria: procedimento.categoria || 'Odontologia'
    };
    this.editandoId = procedimento.id;
    this.formularioAberto = true;
    this.mensagem = '';
    this.erro = '';
  }

  cancelarFormulario(): void {
    this.formularioAberto = false;
    this.editandoId = undefined;
    this.form = this.novoForm();
  }

  salvarProcedimento(): void {
    if (!this.form.nome.trim()) {
      this.erro = 'Informe o nome do procedimento.';
      return;
    }
    if (Number(this.form.valorBase) < 0) {
      this.erro = 'Informe um valor maior ou igual a zero.';
      return;
    }

    this.mensagem = '';
    this.erro = '';
    this.salvandoId = this.editandoId || 0;
    this.procedimentoService.salvar({
      id: this.editandoId,
      nome: this.form.nome.trim(),
      valorBase: Number(this.form.valorBase || 0),
      categoria: this.form.categoria || 'Odontologia'
    }).subscribe({
      next: (salvo) => {
        this.salvandoId = undefined;
        this.mensagem = `Procedimento ${salvo.nome} salvo com sucesso.`;
        const existe = this.procedimentos.some(item => item.id === salvo.id);
        this.procedimentos = existe
          ? this.procedimentos.map(item => item.id === salvo.id ? salvo : item)
          : [...this.procedimentos, salvo];
        this.valores[salvo.id] = Number(salvo.valorBase || 0);
        this.cancelarFormulario();
      },
      error: (err) => {
        this.salvandoId = undefined;
        this.erro = this.mensagemErro(err, 'Erro ao salvar procedimento.');
      }
    });
  }

  private carregarProcedimentos(): void {
    this.carregando = true;
    this.procedimentoService.listar().subscribe({
      next: (procedimentos) => {
        this.procedimentos = procedimentos.filter(item => item.ativo !== false);
        this.valores = {};
        this.procedimentos.forEach(item => this.valores[item.id] = Number(item.valorBase || 0));
        this.carregando = false;
      },
      error: (err) => {
        this.erro = this.mensagemErro(err, 'Erro ao carregar procedimentos.');
        this.carregando = false;
      }
    });
  }

  private mensagemErro(err: any, padrao: string): string {
    if (typeof err?.error === 'string' && err.error.trim()) {
      return err.error;
    }
    return err?.error?.message || padrao;
  }

  private novoForm() {
    return {
      nome: '',
      valorBase: 0,
      categoria: 'Odontologia'
    };
  }
}
