import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { FraudCheckResponse } from '../models/fraud-check-response.model';
import { TransactionFormComponent } from '../components/transaction-form/transaction-form.component';
import { FraudResultComponent } from '../components/fraud-result/fraud-result.component';
import { TransactionHistoryComponent } from '../components/transaction-history/transaction-history.component';

@Component({
  selector: 'app-transaction-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TransactionFormComponent,
    FraudResultComponent,
    TransactionHistoryComponent
  ],
  templateUrl: './transaction-dashboard.component.html',
  styleUrl: './transaction-dashboard.component.css'
})
export class TransactionDashboardComponent {
  private auth = inject(AuthService);

  username = this.auth.getUsername();
  latestResult = signal<FraudCheckResponse | null>(null);

  onTransactionResult(result: FraudCheckResponse): void {
    this.latestResult.set(result);
  }

  logout(): void {
    this.auth.logout();
  }
}
