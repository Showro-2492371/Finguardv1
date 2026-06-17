import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertBannerComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = signal(false);
  errorMsg = signal('');
  showPass = signal(false);
  registered = signal(false);
  portal = signal<'user' | 'admin'>('user');

  form = this.fb.group({
    name:     ['', Validators.required],
    password: ['', Validators.required]
  });

  constructor() {
    this.route.data.subscribe(d => {
      this.portal.set((d['portal'] === 'admin' ? 'admin' : 'user'));
    });

    this.route.queryParams.subscribe(p => {
      if (p['registered'] === 'true') this.registered.set(true);
    });
  }

  togglePasswordVisibility() {
    this.showPass.update(value => !value);
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.errorMsg.set('');
    this.auth.login(this.form.value.name!, this.form.value.password!).subscribe({
      next: () => {
        this.loading.set(false);

        const isAdmin = this.auth.isAdmin();
        if (this.portal() === 'admin' && !isAdmin) {
          this.errorMsg.set('This is admin login. Please use an admin account.');
          this.auth.logout();
          return;
        }
        if (this.portal() === 'user' && isAdmin) {
          this.errorMsg.set('This is user login. Please use the admin login page for admin accounts.');
          this.auth.logout();
          return;
        }

        this.router.navigate([isAdmin ? '/admin/dashboard' : '/user/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        const status = err?.status;
        const message = err?.error?.message || err?.error || '';
        if (status === 403 && message) {
          this.errorMsg.set(message);
          return;
        }
        this.errorMsg.set('Invalid credentials. Please try again.');
      }
    });
  }
}


