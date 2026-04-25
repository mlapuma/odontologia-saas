import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Paciente } from '../../models/paciente.model';
import { FichaPacienteImportacaoService } from '../../services/ficha-paciente-importacao.service';

@Component({
  selector: 'app-importar-ficha',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './importar-ficha.component.html',
  styleUrl: './importar-ficha.component.css'
})
export class ImportarFichaComponent {

  arquivo?: File;
  paciente: Paciente = this.novoPaciente();
  textoExtraido = '';
  aviso = '';
  mensagem = '';
  erro = '';
  carregando = false;
  salvando = false;
  previewGerado = false;

  constructor(private importacaoService: FichaPacienteImportacaoService) { }

  selecionarArquivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.arquivo = input.files?.[0];
    this.mensagem = '';
    this.erro = '';
    this.aviso = '';
    this.previewGerado = false;
  }

  analisarFicha(): void {
    this.mensagem = '';
    this.erro = '';
    this.aviso = '';

    if (!this.arquivo) {
      this.erro = 'Selecione uma ficha escaneada.';
      return;
    }

    this.carregando = true;
    this.importacaoService.preview(this.arquivo).subscribe({
      next: (preview) => {
        this.paciente = {
          ...this.novoPaciente(),
          ...preview.paciente
        };
        this.textoExtraido = preview.textoExtraido || '';
        this.aviso = preview.aviso || '';
        this.previewGerado = true;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao analisar ficha.';
        this.carregando = false;
      }
    });
  }

  salvarPaciente(): void {
    this.mensagem = '';
    this.erro = '';

    if (!this.paciente.nome || this.paciente.nome.trim() === '') {
      this.erro = 'Nome é obrigatório antes de cadastrar.';
      return;
    }

    this.salvando = true;
    this.importacaoService.salvar(this.paciente).subscribe({
      next: (paciente) => {
        this.mensagem = `Paciente cadastrado com sucesso. ID ${paciente.id}`;
        this.salvando = false;
        this.previewGerado = false;
        this.paciente = this.novoPaciente();
        this.textoExtraido = '';
      },
      error: () => {
        this.erro = 'Erro ao cadastrar paciente.';
        this.salvando = false;
      }
    });
  }

  private novoPaciente(): Paciente {
    return {
      nome: '',
      cpf: '',
      telefone: '',
      whatsapp: '',
      email: '',
      dataNascimento: '',
      endereco: '',
      numero: '',
      complemento: '',
      bairro: '',
      cep: '',
      cidade: '',
      uf: '',
      ativo: true
    };
  }
}
