import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FraudAnalyticsDTO } from '../models/analytics.models';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly BASE = '/api/analytics';
  private http = inject(HttpClient);

  /** Triggers a fresh snapshot generation on the server */
  generateSnapshot(): Observable<FraudAnalyticsDTO> {
    return this.http.post<FraudAnalyticsDTO>(`${this.BASE}/generate`, null);
  }

  /** Returns the latest analytics snapshot */
  getLatest(): Observable<FraudAnalyticsDTO> {
    return this.http.get<FraudAnalyticsDTO>(`${this.BASE}/latest`);
  }

  /** Returns up to 12 historical snapshots for trend visualisation */
  getHistory(): Observable<FraudAnalyticsDTO[]> {
    return this.http.get<FraudAnalyticsDTO[]>(`${this.BASE}/history`);
  }
}

