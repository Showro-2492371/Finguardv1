import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { FraudCheckResponse } from '../models/fraud-check-response.model';
import { TransactionRequest } from '../models/transaction-request.model';
import { TransactionHistoryEntry } from '../models/transaction-history-entry.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly BASE_URL = '/api/customer/transaction';
  private http = inject(HttpClient);

  private historySubject = new BehaviorSubject<TransactionHistoryEntry[]>([]);
  history$ = this.historySubject.asObservable();

  createTransaction(request: TransactionRequest): Observable<FraudCheckResponse> {
    return this.http.post<FraudCheckResponse>(`${this.BASE_URL}/add`, request).pipe(
      tap((response: FraudCheckResponse) => {
        const entry: TransactionHistoryEntry = {
          ...response,
          amount: request.amount,
          channel: request.channel,
          transactionType: request.transactionType
        };
        this.historySubject.next([entry, ...this.historySubject.getValue()]);
      })
    );
  }

  detectFraud(request: TransactionRequest): Observable<FraudCheckResponse> {
    return this.http.post<FraudCheckResponse>(`${this.BASE_URL}/detectFraud`, request);
  }

  clearHistory(): void {
    this.historySubject.next([]);
  }
}

