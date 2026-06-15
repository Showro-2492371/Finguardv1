import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { TransactionService } from '../../services/transaction.service';
import { TransactionHistoryEntry } from '../../models/transaction-history-entry.model';
import { TransactionStatus } from '../../models/transaction-request.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './transaction-history.component.html',
  styleUrl: './transaction-history.component.css'
})
export class TransactionHistoryComponent {
  private txService = inject(TransactionService);

  history$: Observable<TransactionHistoryEntry[]> = this.txService.history$;
  readonly TransactionStatus = TransactionStatus;

  clearHistory(): void {
    this.txService.clearHistory();
  }

  badgeClass(status: TransactionStatus): string {
    switch (status) {
      case TransactionStatus.SUCCESS: return 'badge-success';
      case TransactionStatus.FLAGGED: return 'badge-flagged';
      case TransactionStatus.BLOCKED: return 'badge-blocked';
      default: return '';
    }
  }

  parseDate(createdAt: string | number[] | null): Date | null {
    if (!createdAt) return null;
    if (Array.isArray(createdAt)) {
      const [y, mo, d, h, mi, s] = createdAt as number[];
      return new Date(y, mo - 1, d, h, mi, s);
    }
    return new Date(createdAt as string);
  }
}

