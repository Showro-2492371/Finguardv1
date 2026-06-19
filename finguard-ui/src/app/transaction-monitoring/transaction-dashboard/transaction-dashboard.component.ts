import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { FraudCheckResponse } from '../models/fraud-check-response.model';
import { TransactionFormComponent } from '../components/transaction-form/transaction-form.component';
import { FraudResultComponent } from '../components/fraud-result/fraud-result.component';
import { TransactionHistoryComponent } from '../components/transaction-history/transaction-history.component';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';

@Component({
  selector: 'app-transaction-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TransactionFormComponent,
    FraudResultComponent,
    TransactionHistoryComponent,
    NavbarComponent
  ],
  templateUrl: './transaction-dashboard.component.html',
  styleUrl: './transaction-dashboard.component.css'
})
export class TransactionDashboardComponent {
  private auth = inject(AuthService);

  latestResult = signal<FraudCheckResponse | null>(null);

  onTransactionResult(result: FraudCheckResponse): void {
    this.latestResult.set(result);
  }
}
