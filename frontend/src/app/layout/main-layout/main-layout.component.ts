import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  nomeUsuario = localStorage.getItem('usuarioNome') || 'Usuário';
  perfil = localStorage.getItem('perfil') || '';

  constructor(private router: Router) {}

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/']);
  }
}
