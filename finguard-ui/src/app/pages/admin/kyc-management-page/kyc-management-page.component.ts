import { Component, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse } from '@angular/common/http';

import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';
import { KycAdminService } from '../../../services/kyc-admin.service';
import { KycService } from '../../../services/kyc.service';
import { AccountStatus, KycAdminRecord, KycStatus } from '../../../models/kyc.models';
import { parseBackendDate } from '../../../core/utils/date.helper';

@Component({
  selector: 'app-kyc-management-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, AlertBannerComponent],
  templateUrl: './kyc-management-page.component.html',
  styleUrl: './kyc-management-page.component.css'
})
export class KycManagementPageComponent {
  private kycAdmin = inject(KycAdminService);
  private kycService = inject(KycService);
  private platformId = inject(PLATFORM_ID);
  private route = inject(ActivatedRoute);

  loading = signal(false);
  recordsLoading = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  // Section 1
  updateCustomerId = signal('');
  selectedStatus = signal<KycStatus>('IN_PROGRESS');
  statuses: KycStatus[] = ['NOT_STARTED', 'IN_PROGRESS', 'VERIFIED', 'REJECTED'];

  // Section 2
  selectedAccountStatus = signal<AccountStatus>('PENDING');
  accountStatuses: AccountStatus[] = ['PENDING', 'ACTIVE', 'SUSPENDED', 'CLOSED'];

  // Section 3
  downloadCustomerId = signal('');

  // Section 4
  allKycRecords = signal<KycAdminRecord[]>([]);
  parseDate = parseBackendDate;

  constructor() {
    this.loadAllKycRecords();

    this.route.queryParamMap.subscribe(params => {
      const customerId = params.get('customerId');
      if (!customerId) {
        return;
      }
      this.updateCustomerId.set(customerId);
      this.downloadCustomerId.set(customerId);
    });
  }

  updateStatus() {
    const id = Number(this.updateCustomerId());
    if (!id) { this.errorMsg.set('Enter a valid Customer ID.'); return; }
    this.loading.set(true); this.successMsg.set(''); this.errorMsg.set('');
    this.kycAdmin.updateKycStatus(id, this.selectedStatus()).subscribe({
      next: msg => {
        this.loading.set(false);
        this.successMsg.set(msg);
        this.loadAllKycRecords();
      },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Update failed.'); }
    });
  }

  updateAccountStatus() {
    const id = Number(this.updateCustomerId());
    if (!id) { this.errorMsg.set('Enter a valid Customer ID.'); return; }
    this.loading.set(true); this.successMsg.set(''); this.errorMsg.set('');
    this.kycAdmin.updateAccountStatus(id, this.selectedAccountStatus()).subscribe({
      next: msg => {
        this.loading.set(false);
        this.successMsg.set(msg);
        this.loadAllKycRecords();
      },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Account status update failed.'); }
    });
  }

  loadAllKycRecords() {
    this.recordsLoading.set(true);
    this.kycAdmin.getAllKycRecords().subscribe({
      next: records => {
        this.recordsLoading.set(false);
        this.allKycRecords.set(records);
      },
      error: err => {
        this.recordsLoading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'Failed to load KYC records.');
      }
    });
  }

  downloadDoc() {
    const id = Number(this.downloadCustomerId());
    if (!id) { this.errorMsg.set('Enter a valid Customer ID.'); return; }
    this.errorMsg.set(''); this.successMsg.set('');
    this.kycService.downloadDocument(id).subscribe({
      next: (res: HttpResponse<Blob>) => {
        if (isPlatformBrowser(this.platformId)) {
          const blob = res.body;
          if (!blob) {
            this.errorMsg.set('Download failed: empty file received.');
            return;
          }

          const contentDisposition = res.headers.get('content-disposition') || '';
          const filenameMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
          const filename = filenameMatch?.[1] || `kyc-customer-${id}`;

          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = filename;
          a.target = '_self';
          a.rel = 'noopener';
          a.click();
          setTimeout(() => URL.revokeObjectURL(url), 1000);
          this.successMsg.set(`Document download started: ${filename}`);
        }
      },
      error: () => this.errorMsg.set('No KYC document found for this customer.')
    });
  }
}
