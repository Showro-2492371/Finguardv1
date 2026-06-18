import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';

function passwordMatchValidator(ctrl: AbstractControl): ValidationErrors | null {
  const pw = ctrl.get('password')?.value;
  const cp = ctrl.get('confirmPassword')?.value;
  return pw && cp && pw !== cp ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AlertBannerComponent],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = signal(false);
  errorMsg = signal('');
  infoMsg = signal('');
  showPass = signal(false);
  showConfirm = signal(false);
  portal = signal<'user' | 'admin'>('user');
  isAdminPortal = signal(false);

  form = this.fb.group({
    name:            ['', Validators.required],
    contactInfo:     ['', Validators.required],
    password:        ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    mfaEnabled:      [false]
  }, { validators: passwordMatchValidator });

  constructor() {
    this.route.data.subscribe(d => {
      const currentPortal = (d['portal'] === 'admin' ? 'admin' : 'user');
      this.portal.set(currentPortal);
      this.isAdminPortal.set(currentPortal === 'admin');
      if (currentPortal === 'admin') {
        this.infoMsg.set('Admin accounts are created by system administrators only. Use admin login if you already have credentials.');
      } else {
        this.infoMsg.set('');
      }
    });
  }

  togglePasswordVisibility() {
    this.showPass.update(value => !value);
  }

  toggleConfirmVisibility() {
    this.showConfirm.update(value => !value);
  }

  submit() {
    if (this.isAdminPortal()) {
      this.errorMsg.set('Admin self-signup is disabled. Please contact the system administrator.');
      return;
    }
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.errorMsg.set('');
    const { name, contactInfo, password, mfaEnabled } = this.form.value;
    this.auth.signup({ name: name!, contactInfo: contactInfo!, password: password!, mfaEnabled: mfaEnabled! }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate([this.portal() === 'admin' ? '/admin/login' : '/user/login'], {
          queryParams: { registered: 'true' }
        });
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(this.auth.extractApiErrorMessage(err, 'Registration failed. Please try again.'));
      }
    });
  }
}



