import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditTrail, ComplianceReportDTO, ComplianceSummary } from '../models/compliance.models';

@Injectable({ providedIn: 'root' })
export class ComplianceService {
  private readonly BASE = '/api/compliance';
  private http = inject(HttpClient);

  generateAllReports(user: string): Observable<ComplianceReportDTO[]> {
    return this.http.post<ComplianceReportDTO[]>(`${this.BASE}/generate`, null, {
      params: new HttpParams().set('user', user)
    });
  }

  generateForCustomer(id: number, user: string): Observable<ComplianceReportDTO> {
    return this.http.post<ComplianceReportDTO>(`${this.BASE}/generate/${id}`, null, {
      params: new HttpParams().set('user', user)
    });
  }

  getAllReports(): Observable<ComplianceReportDTO[]> {
    return this.http.get<ComplianceReportDTO[]>(`${this.BASE}/allreports`);
  }

  getReportsByCustomer(id: number): Observable<ComplianceReportDTO[]> {
    return this.http.get<ComplianceReportDTO[]>(`${this.BASE}/reports/${id}`);
  }

  getSummary(): Observable<ComplianceSummary> {
    return this.http.get<ComplianceSummary>(`${this.BASE}/summary`);
  }

  exportCsv(id: number, user: string): Observable<string> {
    return this.http.get(`${this.BASE}/export/csv/${id}`, {
      params: new HttpParams().set('user', user),
      responseType: 'text'
    });
  }

  fetchGeneratedCsv(customerId: number): Observable<string> {
    return this.http.get(`${this.BASE}/download-csv/${customerId}`, { responseType: 'text' });
  }

  deleteReport(id: number, user?: string): Observable<string> {
    let params = new HttpParams();
    if (user) params = params.set('user', user);
    return this.http.delete(`${this.BASE}/reports/${id}`, { params, responseType: 'text' });
  }

  filterReports(filters: {
    startDate?: string; endDate?: string; month?: number; year?: number;
  }): Observable<ComplianceReportDTO[]> {
    let params = new HttpParams();
    if (filters.startDate) params = params.set('startDate', filters.startDate);
    if (filters.endDate)   params = params.set('endDate', filters.endDate);
    if (filters.month != null) params = params.set('month', String(filters.month));
    if (filters.year  != null) params = params.set('year', String(filters.year));
    return this.http.get<ComplianceReportDTO[]>(`${this.BASE}/reports/filter`, { params });
  }

  getAuditLogs(): Observable<AuditTrail[]> {
    return this.http.get<AuditTrail[]>(`${this.BASE}/audit-logs`);
  }
}

