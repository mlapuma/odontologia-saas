import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Paciente } from '../../models/paciente.model';
import { PacienteService } from '../../services/paciente.service';


@Component({
  selector: 'app-pacientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pacientes.component.html',
  styleUrl: './pacientes.component.css'
})
export class PacientesComponent implements OnInit {

  pacientes: Paciente[] = [];
  pacienteForm: Paciente = this.novoPaciente();
  editando = false;
  exibindoFormulario = false;
  carregando = false;
  mensagem = '';
  erro = '';
  filtro = '';
  paginaAtual = 1;
  itensPorPagina = 10;

  constructor(private pacienteService: PacienteService) { }

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    this.carregando = true;
    this.pacienteService.listar().subscribe({
      next: (response) => {
        this.pacientes = response;
        this.ajustarPaginaAtual();
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar pacientes.';
        this.carregando = false;
      }
    });
  }

  salvar(): void {
    this.mensagem = '';
    this.erro = '';

    if (!this.pacienteForm.nome || this.pacienteForm.nome.trim() === '') {
      this.erro = 'Nome é obrigatório.';
      return;
    }

    if (this.editando && this.pacienteForm.id) {
      this.pacienteService.atualizar(this.pacienteForm.id, this.pacienteForm).subscribe({
        next: () => {
          this.mensagem = 'Paciente atualizado com sucesso.';
          this.cancelar();
          this.listar();
        },
        error: () => {
          this.erro = 'Erro ao atualizar paciente.';
        }
      });
      return;
    }

    this.pacienteService.salvar(this.pacienteForm).subscribe({
      next: () => {
        this.mensagem = 'Paciente cadastrado com sucesso.';
        this.cancelar();
        this.listar();
      },
      error: () => {
        this.erro = 'Erro ao salvar paciente.';
      }
    });
  }

  editar(paciente: Paciente): void {
    this.pacienteForm = { ...paciente };
    this.editando = true;
    this.exibindoFormulario = true;
    this.mensagem = '';
    this.erro = '';
  }

  novo(): void {
    this.pacienteForm = this.novoPaciente();
    this.editando = false;
    this.exibindoFormulario = true;
    this.mensagem = '';
    this.erro = '';
  }

  excluir(id?: number): void {
    if (!id) {
      return;
    }

    const confirmou = confirm('Deseja realmente excluir este paciente?');
    if (!confirmou) {
      return;
    }

    this.pacienteService.excluir(id).subscribe({
      next: () => {
        this.mensagem = 'Paciente excluído com sucesso.';
        this.listar();
      },
      error: () => {
        this.erro = 'Erro ao excluir paciente.';
      }
    });
  }

  cancelar(): void {
    this.pacienteForm = this.novoPaciente();
    this.editando = false;
    this.exibindoFormulario = false;
  }

  limparFiltro(): void {
    this.filtro = '';
    this.paginaAtual = 1;
  }

  aoFiltrar(): void {
    this.paginaAtual = 1;
  }

  alterarItensPorPagina(valor: number | string): void {
    this.itensPorPagina = Number(valor);
    this.paginaAtual = 1;
  }

  irParaPagina(pagina: number): void {
    if (pagina < 1 || pagina > this.totalPaginas) {
      return;
    }

    this.paginaAtual = pagina;
  }

  get pacientesFiltrados(): Paciente[] {
    const termo = this.normalizarBusca(this.filtro);
    if (!termo) {
      return this.pacientes;
    }

    return this.pacientes.filter(paciente => this.normalizarBusca([
      paciente.nome,
      paciente.cpf,
      paciente.telefone,
      paciente.whatsapp,
      paciente.email,
      paciente.endereco,
      paciente.bairro,
      paciente.cidade,
      paciente.uf
    ].join(' ')).includes(termo));
  }

  get pacientesPaginados(): Paciente[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.pacientesFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.pacientesFiltrados.length / this.itensPorPagina));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, index) => index + 1);
  }

  get primeiroItemExibido(): number {
    if (this.pacientesFiltrados.length === 0) {
      return 0;
    }

    return (this.paginaAtual - 1) * this.itensPorPagina + 1;
  }

  get ultimoItemExibido(): number {
    return Math.min(this.paginaAtual * this.itensPorPagina, this.pacientesFiltrados.length);
  }

  private ajustarPaginaAtual(): void {
    if (this.paginaAtual > this.totalPaginas) {
      this.paginaAtual = this.totalPaginas;
    }
  }

  private normalizarBusca(valor?: string): string {
    return (valor || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }

  private novoPaciente(): Paciente {
    return {
      nome: '',
      cpf: '',
      telefone: '',
      whatsapp: '',
      email: '',
      endereco: '',
      numero: '',
      complemento: '',
      bairro: '',
      cep: '',
      dataNascimento: '',
      cidade: '',
      uf: '',
      ativo: true
    };
  }
}
