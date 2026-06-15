import { Component, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { KycService } from '../../../services/kyc.service';
import { AuthService } from '../../../core/services/auth.service';
import { HttpResponse } from '@angular/common/http';
import { KycStatusResponse } from '../../../models/kyc.models';
import { parseBackendDate } from '../../../core/utils/date.helper';

@Component({
  selector: 'app-kyc-page',
  standalone: true,
  imports: [CommonModule, NavbarComponent, AlertBannerComponent, ConfirmDialogComponent],
  templateUrl: './kyc-page.component.html',
  styleUrl: './kyc-page.component.css'
})
export class KycPageComponent {
  private kycService = inject(KycService);
  private auth = inject(AuthService);
  private platformId = inject(PLATFORM_ID);

  loading = signal(false);
  hasDocument = signal(false);
  status = signal<KycStatusResponse | null>(null);
  successMsg = signal('');
  errorMsg = signal('');
  showDeleteConfirm = signal(false);
  selectedUploadFile = signal<File | null>(null);
  selectedUpdateFile = signal<File | null>(null);

  get customerId() { return this.auth.getCustomerId(); }
  get uploadedAt() { return parseBackendDate(this.status()?.uploadedAt ?? null); }
  get currentStatus() { return this.status()?.kycStatus ?? 'NOT_STARTED'; }
  get canUpload() { return this.currentStatus === 'NOT_STARTED' || this.currentStatus === 'REJECTED'; }
  get canUpdate() { return this.currentStatus === 'REJECTED' && this.hasDocument(); }
  get canDownload() { return this.hasDocument(); }
  get canDelete() { return this.hasDocument() && this.currentStatus !== 'VERIFIED'; }

  constructor() {
    this.loadStatus();
  }

  loadStatus() {
    this.kycService.getStatus(this.customerId).subscribe({
      next: status => {
        this.status.set(status);
        this.hasDocument.set(status.hasDocument);
      },
      error: () => {
        this.status.set({
          customerId: this.customerId,
          kycStatus: 'NOT_STARTED',
          hasDocument: false,
          documentName: null,
          uploadedAt: null
        });
        this.hasDocument.set(false);
      }
    });
  }

  onUploadFileChange(event: Event) {
    const f = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.selectedUploadFile.set(f);
  }

  onUpdateFileChange(event: Event) {
    const f = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.selectedUpdateFile.set(f);
  }

  upload() {
    const f = this.selectedUploadFile();
    if (!f) { this.errorMsg.set('Please select a file.'); return; }
    if (!this.canUpload) { this.errorMsg.set(`KYC upload is not allowed while status is ${this.currentStatus}.`); return; }
    this.loading.set(true); this.errorMsg.set(''); this.successMsg.set('');
    this.kycService.uploadDocument(f, this.customerId).subscribe({
      next: msg => {
        this.loading.set(false);
        this.successMsg.set(msg);
        this.selectedUploadFile.set(null);
        this.loadStatus();
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'Upload failed.');
      }
    });
  }

  update() {
    const f = this.selectedUpdateFile();
    if (!f) { this.errorMsg.set('Please select a file.'); return; }
    if (!this.canUpdate) { this.errorMsg.set('KYC document can only be updated when the current status is REJECTED.'); return; }
    this.loading.set(true); this.errorMsg.set(''); this.successMsg.set('');
    this.kycService.updateDocument(f, this.customerId).subscribe({
      next: msg => {
        this.loading.set(false);
        this.successMsg.set(msg);
        this.selectedUpdateFile.set(null);
        this.loadStatus();
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'Update failed.');
      }
    });
  }

  download() {
    if (!this.canDownload) {
      this.errorMsg.set('No KYC document is available for download.');
      return;
    }
    this.kycService.downloadDocument(this.customerId).subscribe({
      next: (res: HttpResponse<Blob>) => {
        if (isPlatformBrowser(this.platformId)) {
          const blob = res.body;
          if (!blob) {
            this.errorMsg.set('Download failed: empty file received.');
            return;
          }

          const contentDisposition = res.headers.get('content-disposition') || '';
          const filenameMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
          const filename = filenameMatch?.[1] || `kyc-document-${this.customerId}`;

          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = filename;
          a.target = '_self';
          a.rel = 'noopener';
          a.click();
          setTimeout(() => URL.revokeObjectURL(url), 1000);
        }
      },
      error: err => this.errorMsg.set(err?.error?.message || 'Download failed.')
    });
  }

  confirmDelete() { this.showDeleteConfirm.set(true); }
  cancelDelete()  { this.showDeleteConfirm.set(false); }

  deleteDoc() {
    this.showDeleteConfirm.set(false);
    if (!this.canDelete) {
      this.errorMsg.set('Verified KYC document cannot be deleted.');
      return;
    }
    this.loading.set(true); this.errorMsg.set(''); this.successMsg.set('');
    this.kycService.deleteDocument(this.customerId).subscribe({
      next: msg => {
        this.loading.set(false);
        this.successMsg.set(msg);
        this.selectedUploadFile.set(null);
        this.selectedUpdateFile.set(null);
        this.loadStatus();
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || 'Delete failed.');
      }
    });
  }
}





