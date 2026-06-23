import { Component, inject, signal, computed } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';

function passwordStrengthValidator(ctrl: AbstractControl): ValidationErrors | null {
  const pw = ctrl.value;
  if (!pw) return null;
  const valid =
    pw.length >= 8 &&
    /[A-Z]/.test(pw) &&
    /[a-z]/.test(pw) &&
    /[0-9]/.test(pw) &&
    /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw);
  return valid ? null : { passwordStrength: true };
}

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
    contactInfo:     ['', [Validators.required, Validators.minLength(10), Validators.maxLength(10)]],
    password:        ['', [Validators.required, passwordStrengthValidator]],
    confirmPassword: ['', Validators.required],
    mfaEnabled:      [false]
  }, { validators: passwordMatchValidator });

  // Password strength tracking
  passwordValue = signal('');

  passwordChecks = computed(() => {
    const pw = this.passwordValue();
    return {
      hasMinLength: pw.length >= 8,
      hasUpperCase: /[A-Z]/.test(pw),
      hasLowerCase: /[a-z]/.test(pw),
      hasNumber: /[0-9]/.test(pw),
      hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw)
    };
  });

  strengthPercent = computed(() => {
    const checks = this.passwordChecks();
    const passed = Object.values(checks).filter(v => v).length;
    return (passed / 5) * 100;
  });

  strengthLabel = computed(() => {
    const pct = this.strengthPercent();
    if (pct === 0) return '';
    if (pct <= 40) return 'Weak';
    if (pct <= 80) return 'Fair';
    return 'Strong';
  });

  strengthColor = computed(() => {
    const label = this.strengthLabel();
    if (label === 'Weak') return '#dc3545';
    if (label === 'Fair') return '#ffc107';
    if (label === 'Strong') return '#28a745';
    return 'transparent';
  });

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

    this.form.get('password')?.valueChanges.subscribe(val => {
      this.passwordValue.set(val || '');
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
        this.errorMsg.set(err?.error?.message || err?.error || 'Registration failed. Please try again.');
      }
    });
  }
}
