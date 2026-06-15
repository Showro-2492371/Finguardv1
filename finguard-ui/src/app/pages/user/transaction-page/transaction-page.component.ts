import { Component } from '@angular/core';
import { TransactionDashboardComponent } from '../../../transaction-monitoring/transaction-dashboard/transaction-dashboard.component';

@Component({
  selector: 'app-transaction-page',
  standalone: true,
  imports: [TransactionDashboardComponent],
  templateUrl: './transaction-page.component.html',
  styleUrl: './transaction-page.component.css'
})
export class TransactionPageComponent {}


