import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountStatus, KycAdminRecord, KycStatus } from '../models/kyc.models';

@Injectable({ providedIn: 'root' })
export class KycAdminService {
  private http = inject(HttpClient);

  getAllKycRecords(): Observable<KycAdminRecord[]> {
    return this.http.get<KycAdminRecord[]>('/api/admin/kyc/records');
  }

  updateKycStatus(customerId: number, status: KycStatus): Observable<string> {
    const params = new HttpParams()
      .set('customerId', String(customerId))
      .set('status', status);
    return this.http.put('/api/admin/kyc/update-status', null, { params, responseType: 'text' });
  }

  updateAccountStatus(customerId: number, status: AccountStatus): Observable<string> {
    const params = new HttpParams()
      .set('customerId', String(customerId))
      .set('status', status);
    return this.http.put('/api/admin/customer/account-status', null, { params, responseType: 'text' });
  }
}
