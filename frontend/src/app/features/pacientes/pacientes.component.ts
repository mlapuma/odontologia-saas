import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Paciente } from '../../models/paciente.model';
import { CepService } from '../../services/cep.service';
import { PacienteService } from '../../services/paciente.service';


@Component({
  selector: 'app-pacientes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
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
  mensagemCep = '';
  carregandoCep = false;
  filtro = '';
  paginaAtual = 1;
  itensPorPagina = 10;
  private ultimoCepConsultado = '';

  constructor(
    private pacienteService: PacienteService,
    private cepService: CepService
  ) { }

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
    this.pacienteForm = this.aplicarMascarasPaciente({ ...paciente });
    this.editando = true;
    this.exibindoFormulario = true;
    this.mensagem = '';
    this.erro = '';
    this.mensagemCep = '';
    this.ultimoCepConsultado = this.somenteNumeros(this.pacienteForm.cep);
  }

  novo(): void {
    this.pacienteForm = this.novoPaciente();
    this.editando = false;
    this.exibindoFormulario = true;
    this.mensagem = '';
    this.erro = '';
    this.mensagemCep = '';
    this.ultimoCepConsultado = '';
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
    this.mensagemCep = '';
    this.ultimoCepConsultado = '';
  }

  aoAlterarCpf(valor: string): void {
    this.pacienteForm.cpf = this.formatarCpf(valor);
  }

  aoAlterarTelefone(campo: 'telefone' | 'whatsapp', valor: string): void {
    this.pacienteForm[campo] = this.formatarTelefone(valor);
  }

  aoAlterarCep(valor: string): void {
    this.pacienteForm.cep = this.formatarCep(valor);
    this.mensagemCep = '';

    const cepLimpo = this.somenteNumeros(this.pacienteForm.cep);
    if (cepLimpo.length === 8 && cepLimpo !== this.ultimoCepConsultado) {
      this.consultarCep();
    }
  }

  consultarCep(): void {
    const cepLimpo = this.somenteNumeros(this.pacienteForm.cep);
    if (cepLimpo.length !== 8 || this.carregandoCep) {
      return;
    }

    this.carregandoCep = true;
    this.mensagemCep = 'Buscando CEP...';
    this.ultimoCepConsultado = cepLimpo;

    this.cepService.consultar(cepLimpo).subscribe({
      next: (response) => {
        this.carregandoCep = false;

        if (response.erro) {
          this.mensagemCep = 'CEP não encontrado.';
          return;
        }

        this.pacienteForm.endereco = response.logradouro || this.pacienteForm.endereco;
        this.pacienteForm.bairro = response.bairro || this.pacienteForm.bairro;
        this.pacienteForm.cidade = response.localidade || this.pacienteForm.cidade;
        this.pacienteForm.uf = (response.uf || this.pacienteForm.uf || '').toUpperCase();
        this.mensagemCep = 'Endereço preenchido pelo CEP.';
      },
      error: () => {
        this.carregandoCep = false;
        this.mensagemCep = 'Não foi possível consultar o CEP agora.';
      }
    });
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

  private aplicarMascarasPaciente(paciente: Paciente): Paciente {
    return {
      ...paciente,
      cpf: this.formatarCpf(paciente.cpf),
      telefone: this.formatarTelefone(paciente.telefone),
      whatsapp: this.formatarTelefone(paciente.whatsapp),
      cep: this.formatarCep(paciente.cep),
      uf: (paciente.uf || '').toUpperCase()
    };
  }

  private formatarCpf(valor?: string): string {
    const numeros = this.somenteNumeros(valor).slice(0, 11);
    return numeros
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  private formatarTelefone(valor?: string): string {
    const numeros = this.somenteNumeros(valor).slice(0, 11);
    if (numeros.length <= 10) {
      return numeros
        .replace(/(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{4})(\d)/, '$1-$2');
    }

    return numeros
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{5})(\d)/, '$1-$2');
  }

  private formatarCep(valor?: string): string {
    return this.somenteNumeros(valor)
      .slice(0, 8)
      .replace(/(\d{5})(\d)/, '$1-$2');
  }

  private somenteNumeros(valor?: string): string {
    return (valor || '').replace(/\D/g, '');
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
