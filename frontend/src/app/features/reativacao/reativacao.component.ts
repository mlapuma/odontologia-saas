import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DashboardResumo, PacienteSemRetorno } from '../../models/dashboard-resumo.model';
import { DashboardService } from '../../services/dashboard.service';
import { WhatsappRequest, WhatsappService } from '../../services/whatsapp.service';

@Component({
  selector: 'app-reativacao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reativacao.component.html',
  styleUrl: './reativacao.component.css'
})
export class ReativacaoComponent implements OnInit {
  resumo?: DashboardResumo;
  carregando = false;
  erro = '';
  aviso = '';
  mensagemSelecionada = 'limpeza';
  modelosMensagem = [
    {
      id: 'limpeza',
      nome: 'Promocao de limpeza',
      texto: 'Ola, {nome}! Tudo bem? Estamos com uma condicao especial para limpeza odontologica na Clinica LaCari. Quer que eu veja um horario para voce?'
    },
    {
      id: 'generica',
      nome: 'Mensagem generica',
      texto: 'Ola, {nome}! Tudo bem? Sentimos sua falta aqui na Clinica LaCari. Gostaria de agendar uma avaliacao ou retorno?'
    },
    {
      id: 'retorno',
      nome: 'Convite para retorno',
      texto: 'Ola, {nome}! Ja faz um tempo desde seu ultimo atendimento. Podemos agendar uma consulta de acompanhamento para cuidar do seu sorriso?'
    }
  ];

  constructor(
    private dashboardService: DashboardService,
    private whatsappService: WhatsappService
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.erro = '';

    this.dashboardService.resumo().subscribe({
      next: (response) => {
        this.resumo = response;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Erro ao carregar pacientes para reativar.';
        this.carregando = false;
      }
    });
  }

  enviarWhatsapp(paciente: PacienteSemRetorno): void {
    this.aviso = '';
    this.erro = '';

    const modelo = this.modelosMensagem.find(item => item.id === this.mensagemSelecionada) || this.modelosMensagem[0];
    const mensagem = modelo.texto.replace('{nome}', paciente.nome);
    const envio = this.whatsappService.enviar({
      telefone: paciente.whatsapp,
      mensagem,
      tipo: 'REATIVACAO',
      pacienteId: paciente.pacienteId
    });

    if (!envio) {
      this.aviso = `O paciente ${paciente.nome} nao possui WhatsApp cadastrado.`;
      return;
    }

    envio.subscribe({
      next: (notificacao) => {
        if (notificacao.status === 'ENVIADO') {
          this.aviso = `Mensagem enviada automaticamente para ${paciente.nome}.`;
        } else {
          this.abrirNoWhatsappDesktop({
            telefone: paciente.whatsapp,
            mensagem,
            tipo: 'REATIVACAO',
            pacienteId: paciente.pacienteId
          }, notificacao.resposta);
        }
      },
      error: () => {
        this.abrirNoWhatsappDesktop({
          telefone: paciente.whatsapp,
          mensagem,
          tipo: 'REATIVACAO',
          pacienteId: paciente.pacienteId
        }, 'Envio automatico indisponivel.');
      }
    });
  }

  private abrirNoWhatsappDesktop(request: WhatsappRequest, motivo?: string): void {
    if (!this.whatsappService.abrirDesktop(request)) {
      this.erro = 'Nao foi possivel abrir o WhatsApp Desktop. Confira o telefone cadastrado.';
      return;
    }

    this.aviso = `${motivo || 'Envio automatico indisponivel.'} WhatsApp Desktop aberto com a mensagem pronta.`;
  }
}
