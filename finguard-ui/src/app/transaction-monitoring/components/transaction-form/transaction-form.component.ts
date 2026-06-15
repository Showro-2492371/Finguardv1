import { Component, inject, signal, output } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { TransactionService } from '../../services/transaction.service';
import { FraudCheckResponse } from '../../models/fraud-check-response.model';
import { ChannelType, TransactionType } from '../../models/transaction-request.model';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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
  lastResult = signal<FraudCheckResponse | null>(null);

  channelOptions = [
    { value: ChannelType.UPI, label: 'UPI' },
    { value: ChannelType.ONLINE_BANKING, label: 'Online Banking' },
    { value: ChannelType.ATM, label: 'ATM' }
  ];

  typeOptions = [
    { value: TransactionType.CREDITED, label: 'Credit (Money In)' },
    { value: TransactionType.DEBITED, label: 'Debit (Money Out)' }
  ];

  form = this.fb.group({
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    channel: ['' as ChannelType | '', Validators.required],
    transactionType: ['' as TransactionType | '', Validators.required]
  });

  private buildRequest() {
    return {
      customerId: this.auth.getCustomerId(),
      amount: this.form.value.amount!,
      channel: this.form.value.channel as ChannelType,
      transactionType: this.form.value.transactionType as TransactionType
    };
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');

    this.txService.createTransaction(this.buildRequest()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.lastResult.set(res);
        this.transactionResult.emit(res);
        this.form.reset();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'An unexpected error occurred');
      }
    });
  }

  checkFraud(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');

    this.txService.detectFraud(this.buildRequest()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.lastResult.set(res);
        this.transactionResult.emit(res);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'An unexpected error occurred');
      }
    });
  }
}

