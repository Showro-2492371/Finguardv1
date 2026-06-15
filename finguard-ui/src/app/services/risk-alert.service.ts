import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RiskAlertResponse, RiskAlertStatus } from '../models/risk-alert.models';

@Injectable({ providedIn: 'root' })
export class RiskAlertService {
  private readonly BASE = '/api/admin/risk';
  private http = inject(HttpClient);

  getAlerts(status?: RiskAlertStatus): Observable<RiskAlertResponse[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<RiskAlertResponse[]>(`${this.BASE}/alerts`, { params });
  }

  getAlertById(id: number): Observable<RiskAlertResponse> {
    return this.http.get<RiskAlertResponse>(`${this.BASE}/alerts/${id}`);
  }
}

