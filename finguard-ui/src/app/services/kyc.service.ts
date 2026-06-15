import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { KycStatusResponse } from '../models/kyc.models';

@Injectable({ providedIn: 'root' })
export class KycService {
  private readonly BASE = '/api/customer/kyc';
  private http = inject(HttpClient);

  uploadDocument(file: File, customerId: number): Observable<string> {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('customerId', String(customerId));
    return this.http.post(`${this.BASE}/upload`, fd, { responseType: 'text' });
  }

  getStatus(customerId: number): Observable<KycStatusResponse> {
    return this.http.get<KycStatusResponse>(`${this.BASE}/status/${customerId}`);
  }

  downloadDocument(customerId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.BASE}/download/${customerId}`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  updateDocument(file: File, customerId: number): Observable<string> {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('customerId', String(customerId));
    return this.http.put(`${this.BASE}/update-document`, fd, { responseType: 'text' });
  }

  deleteDocument(customerId: number): Observable<string> {
    return this.http.delete(`${this.BASE}/delete/${customerId}`, { responseType: 'text' });
  }
}



