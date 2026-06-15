import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  errorMsg = signal('');
  showPassword = signal(false);

  form = this.fb.group({
    name: ['', Validators.required],
    password: ['', Validators.required]
  });

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMsg.set('');
    const { name, password } = this.form.value;

    this.auth.login(name!, password!).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/transaction']);
      },
      error: () => {
        this.loading.set(false);
        this.errorMsg.set('Invalid credentials. Please try again.');
      }
    });
  }
}

