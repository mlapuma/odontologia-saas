import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DashboardResumo, ProximoAgendamento } from '../../models/dashboard-resumo.model';
import { Profissional } from '../../models/profissional.model';
import { DashboardService } from '../../services/dashboard.service';
import { ProfissionalService } from '../../services/profissional.service';
import { WhatsappRequest, WhatsappService } from '../../services/whatsapp.service';

@Component({
  selector: 'app-proximos-agendamentos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './proximos-agendamentos.component.html',
  styleUrl: './proximos-agendamentos.component.css'
})
export class ProximosAgendamentosComponent implements OnInit {
  resumo?: DashboardResumo;
  profissionais: Profissional[] = [];
  profissionalFiltro: number | null = null;
  carregando = false;
  erro = '';
  aviso = '';

  constructor(
    private dashboardService: DashboardService,
    private profissionalService: ProfissionalService,
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
        this.erro = 'Erro ao carregar próximos agendamentos.';
        this.carregando = false;
      }
    });

    this.profissionalService.listar().subscribe({
      next: (response) => this.profissionais = response.filter(item => item.ativo !== false)
    });
  }

  get agendamentosFiltrados() {
    const agendamentos = this.resumo?.proximosAgendamentos || [];
    if (!this.profissionalFiltro) {
      return agendamentos;
    }

    return agendamentos.filter(agendamento => Number(agendamento.profissionalId) === Number(this.profissionalFiltro));
  }

  compartilharAgendaWhatsapp(): void {
    this.aviso = '';
    this.erro = '';

    const profissional = this.profissionalSelecionado;
    if (!profissional) {
      this.aviso = 'Selecione um profissional para compartilhar a agenda.';
      return;
    }

    if (this.agendamentosFiltrados.length === 0) {
      this.aviso = 'Nao ha atendimentos para compartilhar com este profissional.';
      return;
    }

    const mensagem = this.montarMensagemAgenda(profissional);
    const envio = this.whatsappService.enviar({
      telefone: profissional.telefone,
      mensagem,
      tipo: 'AGENDA_PROFISSIONAL'
    });

    if (!envio) {
      this.aviso = 'Cadastre o telefone do profissional antes de compartilhar a agenda.';
      return;
    }

    envio.subscribe({
      next: (notificacao) => {
        if (notificacao.status === 'ENVIADO') {
          this.aviso = 'Agenda enviada automaticamente pelo WhatsApp.';
        } else {
          this.abrirNoWhatsappDesktop({
            telefone: profissional.telefone,
            mensagem,
            tipo: 'AGENDA_PROFISSIONAL'
          }, notificacao.resposta);
        }
      },
      error: () => {
        this.abrirNoWhatsappDesktop({
          telefone: profissional.telefone,
          mensagem,
          tipo: 'AGENDA_PROFISSIONAL'
        }, 'Envio automatico indisponivel.');
      }
    });
  }

  confirmarAtendimentoWhatsapp(agendamento: ProximoAgendamento): void {
    this.aviso = '';
    this.erro = '';

    const mensagem = this.montarMensagemConfirmacao(agendamento);
    const envio = this.whatsappService.enviar({
      telefone: agendamento.pacienteWhatsapp,
      mensagem,
      tipo: 'CONFIRMACAO',
      pacienteId: agendamento.pacienteId,
      agendamentoId: agendamento.id
    });

    if (!envio) {
      this.aviso = `O paciente ${agendamento.pacienteNome} nao possui WhatsApp cadastrado.`;
      return;
    }

    envio.subscribe({
      next: (notificacao) => {
        if (notificacao.status === 'ENVIADO') {
          this.aviso = `Confirmacao enviada automaticamente para ${agendamento.pacienteNome}.`;
        } else {
          this.abrirNoWhatsappDesktop({
            telefone: agendamento.pacienteWhatsapp,
            mensagem,
            tipo: 'CONFIRMACAO',
            pacienteId: agendamento.pacienteId,
            agendamentoId: agendamento.id
          }, notificacao.resposta);
        }
      },
      error: () => {
        this.abrirNoWhatsappDesktop({
          telefone: agendamento.pacienteWhatsapp,
          mensagem,
          tipo: 'CONFIRMACAO',
          pacienteId: agendamento.pacienteId,
          agendamentoId: agendamento.id
        }, 'Envio automatico indisponivel.');
      }
    });
  }

  get profissionalSelecionado(): Profissional | undefined {
    if (!this.profissionalFiltro) {
      return undefined;
    }
    return this.profissionais.find(profissional => Number(profissional.id) === Number(this.profissionalFiltro));
  }

  private abrirNoWhatsappDesktop(request: WhatsappRequest, motivo?: string): void {
    if (!this.whatsappService.abrirDesktop(request)) {
      this.erro = 'Nao foi possivel abrir o WhatsApp Desktop. Confira o telefone cadastrado.';
      return;
    }

    this.aviso = `${motivo || 'Envio automatico indisponivel.'} WhatsApp Desktop aberto com a mensagem pronta.`;
  }

  private montarMensagemAgenda(profissional: Profissional): string {
    const linhas = [
      `Ola, ${profissional.nome}.`,
      '',
      'Segue sua agenda de proximos atendimentos:',
      ''
    ];

    this.agendamentosFiltrados.forEach((agendamento, index) => {
      linhas.push(`${index + 1}. ${agendamento.pacienteNome}`);
      linhas.push(`Data: ${this.formatarDataHora(agendamento.dataHoraInicio)}`);
      linhas.push(`Tratamento: ${agendamento.tratamento || 'Nao informado'}`);
      linhas.push(`Status: ${agendamento.status}`);
      linhas.push('');
    });

    linhas.push('Clinica LaCari Odontologia');
    return linhas.join('\n');
  }

  private montarMensagemConfirmacao(agendamento: ProximoAgendamento): string {
    return [
      `Ola, ${agendamento.pacienteNome}!`,
      'Podemos confirmar seu atendimento na Clinica LaCari Odontologia?',
      `Data: ${this.formatarDataHora(agendamento.dataHoraInicio)}`,
      `Tratamento: ${agendamento.tratamento || 'Nao informado'}`,
      'Responda SIM para confirmar ou NAO para cancelar.'
    ].join('\n');
  }

  private formatarDataHora(valor: string): string {
    const data = new Date(valor);
    if (Number.isNaN(data.getTime())) {
      return valor;
    }

    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(data);
  }
}
