import { Component, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { TransactionService } from '../../services/transaction.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { parseBackendDate } from '../../core/utils/date.helper';

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [CommonModule, DatePipe, StatusBadgeComponent],
  templateUrl: './transaction-history.component.html',
  styleUrl: './transaction-history.component.css'
})
export class TransactionHistoryComponent {
  private txService = inject(TransactionService);
  history$ = this.txService.history$;
  parseDate = parseBackendDate;
  clear() { this.txService.clearHistory(); }
}

