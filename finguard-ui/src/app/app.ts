import { DOCUMENT } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
  styles: []
})
export class App {
  private router = inject(Router);
  private document = inject(DOCUMENT);

  constructor() {
    this.applyPortalTheme(this.router.url || '/user/login');

    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(event => {
      this.applyPortalTheme((event as NavigationEnd).urlAfterRedirects);
    });
  }

  private applyPortalTheme(url: string) {
    const body = this.document?.body;
    if (!body) {
      return;
    }

    const isAdminPortal = url.startsWith('/admin');
    body.classList.toggle('portal-admin', isAdminPortal);
    body.classList.toggle('portal-user', !isAdminPortal);
  }
}

