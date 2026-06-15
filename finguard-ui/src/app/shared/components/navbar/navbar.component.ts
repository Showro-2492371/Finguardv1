import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { signal } from '@angular/core';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  @Input() role: 'user' | 'admin' = 'user';
  auth = inject(AuthService);
  menuOpen = signal(false);

  toggleMenu() { this.menuOpen.update(v => !v); }
  logout() { this.auth.logout(); }
}


