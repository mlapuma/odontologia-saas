import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, DatesSetArg, DateSelectArg, EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { AgendaService } from '../../services/agenda.service';
import { AgendaEvento } from '../../models/agenda-evento.model';


@Component({
  selector: 'app-agenda',
  standalone: true,
  imports: [CommonModule, FullCalendarModule],
  templateUrl: './agenda.component.html',
  styleUrl: './agenda.component.scss'
})
export class AgendaComponent {

  eventos: AgendaEvento[] = [];

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    locale: 'pt-br',
    selectable: true,
    editable: false,
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
    datesSet: (arg) => this.carregarEventos(arg),
    select: (arg) => this.novoAgendamento(arg),
    eventClick: (arg) => this.detalharEvento(arg)
  };

  constructor(private agendaService: AgendaService) { }

  carregarEventos(arg: DatesSetArg): void {
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
      }
    });
  }

  novoAgendamento(arg: DateSelectArg): void {
    alert(`Novo agendamento em: ${arg.startStr}`);
  }

  detalharEvento(arg: EventClickArg): void {
    alert(`Agendamento selecionado: ${arg.event.title}`);
  }
}