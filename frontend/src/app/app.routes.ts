import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { PacientesComponent } from './features/pacientes/pacientes.component';
import { AgendaComponent } from './features/agenda/agenda.component';
import { FinanceiroComponent } from './features/financeiro/financeiro.component';
import { TratamentosComponent } from './features/tratamentos/tratamentos.component';
import { ImportarFichaComponent } from './features/importar-ficha/importar-ficha.component';
import { IntegracoesComponent } from './features/integracoes/integracoes.component';
import { GooglePerformanceComponent } from './features/google-performance/google-performance.component';
import { ReativacaoComponent } from './features/reativacao/reativacao.component';
import { ProximosAgendamentosComponent } from './features/proximos-agendamentos/proximos-agendamentos.component';
import { ProfissionaisComponent } from './features/profissionais/profissionais.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  {
    path: 'app',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'pacientes', component: PacientesComponent },
      { path: 'importar-ficha', component: ImportarFichaComponent },
      { path: 'agenda', component: AgendaComponent },
      { path: 'tratamentos', component: TratamentosComponent },
      { path: 'financeiro', component: FinanceiroComponent },
      { path: 'integracoes', component: IntegracoesComponent },
      { path: 'google', component: GooglePerformanceComponent },
      { path: 'reativacao', component: ReativacaoComponent },
      { path: 'proximos-agendamentos', component: ProximosAgendamentosComponent },
      { path: 'profissionais', component: ProfissionaisComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];
