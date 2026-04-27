import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, DatesSetArg, DateSelectArg, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { AgendaService } from '../../services/agenda.service';
import { AgendaEvento } from '../../models/agenda-evento.model';
import { AgendaModalComponent } from './agenda-modal/agenda-modal.component';


@Component({
  selector: 'app-agenda',
  standalone: true,
  imports: [CommonModule, FullCalendarModule, AgendaModalComponent],
  templateUrl: './agenda.component.html',
  styleUrl: './agenda.component.css'
})
export class AgendaComponent {

  eventos: AgendaEvento[] = [];
  modalAberto = false;
  modalInicio = '';
  modalAgendamentoId?: number | null;
  ultimaJanela?: DatesSetArg;
  mensagem = '';
  erro = '';

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: this.visaoInicialAgenda(),
    locale: 'pt-br',
    selectable: true,
    editable: false,
    height: 'auto',
    contentHeight: 'auto',
    expandRows: true,
    dayMaxEvents: true,
    allDaySlot: false,
    slotMinTime: '08:00:00',
    slotMaxTime: '18:00:00',
    slotDuration: '00:30:00',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'timeGridDay,timeGridWeek,dayGridMonth'
    },
    events: [],
    datesSet: (arg) => {
      this.ultimaJanela = arg;
      this.carregarEventos(arg);
    },
    select: (arg) => this.novoAgendamento(arg),
    eventClick: (arg) => this.detalharEvento(arg),
    windowResize: (arg) => {
      arg.view.calendar.changeView(this.visaoInicialAgenda());
    }
  };

  constructor(private agendaService: AgendaService) { }

  carregarEventos(arg: DatesSetArg): void {
    this.erro = '';
    const inicio = arg.start.toISOString().slice(0, 19);
    const fim = arg.end.toISOString().slice(0, 19);

    this.agendaService.listarCalendario(inicio, fim).subscribe({
      next: (response) => {
        this.eventos = response;

        this.calendarOptions = {
          ...this.calendarOptions,
          events: this.eventos.map(e => ({
            id: String(e.id),
            title: e.title,
            start: e.start,
            end: e.end,
            color: e.color
          }))
        };
      },
      error: (err) => {
        console.error('Erro ao carregar agenda', err);
        this.erro = 'Erro ao carregar agenda.';
      }
    });
  }

  novoAgendamento(arg: DateSelectArg): void {
    this.mensagem = '';
    this.modalAgendamentoId = null;
    this.modalInicio = this.formatarDataLocal(arg.start);
    this.modalAberto = true;
  }

  detalharEvento(arg: EventClickArg): void {
    this.mensagem = '';
    this.modalInicio = '';
    this.modalAgendamentoId = Number(arg.event.id);
    this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
    this.modalAgendamentoId = null;
    this.modalInicio = '';
  }

  agendamentoSalvo(): void {
    this.mensagem = 'Agendamento salvo com sucesso.';
    this.fecharModal();

    if (this.ultimaJanela) {
      this.carregarEventos(this.ultimaJanela);
    }
  }

  private formatarDataLocal(data: Date): string {
    const ano = data.getFullYear();
    const mes = this.doisDigitos(data.getMonth() + 1);
    const dia = this.doisDigitos(data.getDate());
    const hora = this.doisDigitos(data.getHours());
    const minuto = this.doisDigitos(data.getMinutes());
    return `${ano}-${mes}-${dia}T${hora}:${minuto}`;
  }

  private doisDigitos(valor: number): string {
    return String(valor).padStart(2, '0');
  }

  private visaoInicialAgenda(): string {
    return window.innerWidth <= 720 ? 'timeGridDay' : 'timeGridWeek';
  }
}
