import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FraudCheckResponse } from '../../models/fraud-check-response.model';
import { TransactionStatus } from '../../models/transaction-request.model';

@Component({
  selector: 'app-fraud-result',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './fraud-result.component.html',
  styleUrl: './fraud-result.component.css'
})
export class FraudResultComponent implements OnChanges {
  @Input() result: FraudCheckResponse | null = null;

  fraudReasons: string[] = [];
  progressWidth = 0;
  parsedDate: Date | null = null;

  readonly TransactionStatus = TransactionStatus;

  ngOnChanges(): void {
    if (!this.result) return;

    // Parse fraud reasons
    this.fraudReasons = this.result.fraudReason
      ? this.result.fraudReason.split(', ').filter(r => r.trim().length > 0)
      : [];

    // Progress bar capped at 100
    this.progressWidth = Math.min(this.result.riskScore, 100);

    // Parse createdAt — backend can return ISO string or [y,m,d,h,min,sec,nano] array
    if (this.result.createdAt) {
      if (Array.isArray(this.result.createdAt)) {
        const [y, mo, d, h, mi, s] = this.result.createdAt as number[];
        this.parsedDate = new Date(y, mo - 1, d, h, mi, s);
      } else {
        this.parsedDate = new Date(this.result.createdAt as string);
      }
    } else {
      this.parsedDate = null;
    }
  }

  get barColor(): string {
    const score = this.result?.riskScore ?? 0;
    if (score >= 60) return '#dc3545';
    if (score >= 30) return '#ffc107';
    return '#28a745';
  }

  get badgeClass(): string {
    switch (this.result?.status) {
      case TransactionStatus.SUCCESS: return 'badge-success';
      case TransactionStatus.FLAGGED: return 'badge-flagged';
      case TransactionStatus.BLOCKED: return 'badge-blocked';
      default: return '';
    }
  }
}

