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
  carregando = false;
  mensagem = '';
  erro = '';
  filtro = '';

  constructor(private pacienteService: PacienteService) { }

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    this.carregando = true;
    this.pacienteService.listar().subscribe({
      next: (response) => {
        this.pacientes = response;
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
  }

  limparFiltro(): void {
    this.filtro = '';
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
