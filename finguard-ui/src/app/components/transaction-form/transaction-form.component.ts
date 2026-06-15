import { Component, inject, signal, output } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { TransactionService } from '../../services/transaction.service';
import { FraudCheckResponse } from '../../models/transaction.models';
import { AlertBannerComponent } from '../../shared/components/alert-banner/alert-banner.component';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AlertBannerComponent],
  templateUrl: './transaction-form.component.html',
  styleUrl: './transaction-form.component.css'
})
export class TransactionFormComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private txService = inject(TransactionService);

  transactionResult = output<FraudCheckResponse>();
  loading = signal(false);
  errorMsg = signal('');

  channels = [
    { value: 'UPI', label: 'UPI' },
    { value: 'ONLINE_BANKING', label: 'Online Banking' },
    { value: 'ATM', label: 'ATM' }
  ];

  form = this.fb.group({
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    channel: ['', Validators.required],
    transactionType: ['', Validators.required]
  });

  private buildReq() {
    return {
      customerId: this.auth.getCustomerId(),
      amount: this.form.value.amount!,
      channel: this.form.value.channel as any,
      transactionType: this.form.value.transactionType as any
    };
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.errorMsg.set('');
    this.txService.createTransaction(this.buildReq()).subscribe({
      next: res => {
        this.loading.set(false);
        this.transactionResult.emit(res);
        this.form.reset();
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'An unexpected error occurred');
      }
    });
  }

  checkFraud() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true); this.errorMsg.set('');
    this.txService.detectFraud(this.buildReq()).subscribe({
      next: res => { this.loading.set(false); this.transactionResult.emit(res); },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'An unexpected error occurred');
      }
    });
  }
}

