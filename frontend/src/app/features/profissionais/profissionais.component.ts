import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Profissional } from '../../models/profissional.model';
import { ProfissionalService } from '../../services/profissional.service';

@Component({
  selector: 'app-profissionais',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profissionais.component.html',
  styleUrl: './profissionais.component.css'
})
export class ProfissionaisComponent implements OnInit {
  profissionais: Profissional[] = [];
  form = this.novoForm();
  especialidades = [
    'Clínico Geral',
    'Ortodontia',
    'Implantodontia',
    'Endodontia',
    'Periodontia',
    'Prótese Dentária',
    'Dentística',
    'Odontopediatria',
    'Cirurgia Bucomaxilofacial',
    'Radiologia Odontológica',
    'Estomatologia',
    'Harmonização Orofacial'
  ];
  editandoId?: number;
  carregando = false;
  salvando = false;
  mensagem = '';
  erro = '';

  constructor(private profissionalService: ProfissionalService) {}

  ngOnInit(): void {
    this.listar();
  }

  salvar(): void {
    this.mensagem = '';
    this.erro = '';

    if (!this.form.nome.trim()) {
      this.erro = 'Informe o nome do profissional.';
      return;
    }

    this.salvando = true;
    const request = this.editandoId
      ? this.profissionalService.atualizar(this.editandoId, this.form)
      : this.profissionalService.salvar(this.form);

    request.subscribe({
      next: () => {
        this.salvando = false;
        this.mensagem = this.editandoId ? 'Profissional atualizado com sucesso.' : 'Profissional cadastrado com sucesso.';
        this.cancelarEdicao();
        this.listar();
      },
      error: () => {
        this.salvando = false;
        this.erro = 'Erro ao salvar profissional.';
      }
    });
  }

  editar(profissional: Profissional): void {
    this.editandoId = profissional.id;
    this.form = { ...profissional };
    this.mensagem = '';
    this.erro = '';
  }

  excluir(profissional: Profissional): void {
    if (!profissional.id) {
      return;
    }

    const confirmou = confirm(`Deseja excluir ${profissional.nome}?`);
    if (!confirmou) {
      return;
    }

    this.profissionalService.excluir(profissional.id).subscribe({
      next: () => {
        this.mensagem = 'Profissional excluído com sucesso.';
        if (this.editandoId === profissional.id) {
          this.cancelarEdicao();
        }
        this.listar();
      },
      error: () => {
        this.erro = 'Erro ao excluir profissional.';
      }
    });
  }

  cancelarEdicao(): void {
    this.editandoId = undefined;
    this.form = this.novoForm();
  }

  private listar(): void {
    this.carregando = true;
    this.profissionalService.listar().subscribe({
      next: (response) => {
        this.profissionais = response;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar profissionais.';
        this.carregando = false;
      }
    });
  }

  private novoForm(): Profissional {
    return {
      nome: '',
      especialidade: '',
      cro: '',
      telefone: '',
      email: '',
      ativo: true
    };
  }
}
