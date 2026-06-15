import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { FraudCheckResponse, TransactionHistoryEntry, TransactionRequest } from '../models/transaction.models';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly BASE = '/api/customer/transaction';
  private http = inject(HttpClient);

  private historySubject = new BehaviorSubject<TransactionHistoryEntry[]>([]);
  history$ = this.historySubject.asObservable();

  createTransaction(req: TransactionRequest): Observable<FraudCheckResponse> {
    return this.http.post<FraudCheckResponse>(`${this.BASE}/add`, req).pipe(
      tap((res) => {
        const entry: TransactionHistoryEntry = {
          ...res,
          amount: req.amount,
          channel: req.channel,
          transactionType: req.transactionType
        };
        this.historySubject.next([entry, ...this.historySubject.getValue()]);
      })
    );
  }

  detectFraud(req: TransactionRequest): Observable<FraudCheckResponse> {
    return this.http.post<FraudCheckResponse>(`${this.BASE}/detectFraud`, req);
  }

  clearHistory(): void {
    this.historySubject.next([]);
  }
}

