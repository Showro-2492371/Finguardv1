import { Component, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { ComplianceService } from '../../../services/compliance.service';
import { AuthService } from '../../../core/services/auth.service';
import { AuditTrail, ComplianceReportDTO } from '../../../models/compliance.models';
import { parseBackendDate } from '../../../core/utils/date.helper';

export interface CsvPreview {
  customerId: number;
  filePath: string;
  rows: ComplianceReportDTO[];
  rawCsv: string;
}

@Component({
  selector: 'app-compliance-page',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, NavbarComponent, AlertBannerComponent,
    LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './compliance-page.component.html',
  styleUrl: './compliance-page.component.css'
})
export class CompliancePageComponent {
  private svc = inject(ComplianceService);
  private auth = inject(AuthService);
  private platformId = inject(PLATFORM_ID);
  parseDate = parseBackendDate;

  activeTab = signal<'all'|'generate'|'filter'|'audit'>('all');
  loading = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  // Tab 1
  allReports = signal<ComplianceReportDTO[]>([]);
  deleteTarget = signal<number | null>(null);
  csvPreview = signal<CsvPreview | null>(null);
  csvLoading = signal(false);

  // Tab 2
  genAllResult = signal<ComplianceReportDTO[]>([]);
  genCustomerId = signal('');
  genSingleResult = signal<ComplianceReportDTO | null>(null);

  // Tab 3
  filterStart = signal(''); filterEnd = signal('');
  filterMonth = signal(''); filterYear = signal('');
  filterResult = signal<ComplianceReportDTO[]>([]);

  // Tab 4
  auditLogs = signal<AuditTrail[]>([]);

  get user() { return this.auth.getUsername(); }

  switchTab(tab: 'all'|'generate'|'filter'|'audit') {
    this.activeTab.set(tab);
    this.successMsg.set(''); this.errorMsg.set('');
    this.csvPreview.set(null);
    if (tab === 'all')   this.loadAll();
    if (tab === 'audit') this.loadAudit();
  }

  loadAll() {
    this.loading.set(true);
    this.svc.getAllReports().subscribe({
      next: r => { this.loading.set(false); this.allReports.set(r); },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Failed to load reports.'); }
    });
  }

  exportCsv(customerId: number) {
    this.csvPreview.set(null);
    this.csvLoading.set(true);
    this.successMsg.set('');
    this.errorMsg.set('');

    forkJoin({
      path: this.svc.exportCsv(customerId, this.user),
      rows: this.svc.getReportsByCustomer(customerId)
    }).subscribe({
      next: ({ path, rows }) => {
        this.csvLoading.set(false);
        const rawCsv = this.buildCsvString(rows);
        this.csvPreview.set({ customerId, filePath: path, rows, rawCsv });
        this.successMsg.set(`CSV generated on server: ${path}`);
      },
      error: err => {
        this.csvLoading.set(false);
        this.errorMsg.set(err?.error?.message || 'Export failed.');
      }
    });
  }

  private buildCsvString(rows: ComplianceReportDTO[]): string {
    const header = 'ReportId,CustomerId,FraudCases,RiskScore,GeneratedDate';
    const lines = rows.map(r => {
      const d = parseBackendDate(r.generatedDate);
      const dateStr = d ? d.toISOString().replace('T', ' ').slice(0, 19) : '';
      return `${r.reportId},${r.customerId},${r.fraudCases},${r.riskScore},${dateStr}`;
    });
    return [header, ...lines].join('\n');
  }

  downloadCsvLocally(preview: CsvPreview) {
    if (!isPlatformBrowser(this.platformId)) return;
    const blob = new Blob([preview.rawCsv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `report_customer_${preview.customerId}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  closeCsvPreview() {
    this.csvPreview.set(null);
  }

  askDelete(id: number) { this.deleteTarget.set(id); }
  cancelDelete()       { this.deleteTarget.set(null); }
  confirmDelete() {
    const id = this.deleteTarget();
    if (!id) return;
    this.deleteTarget.set(null);
    this.svc.deleteReport(id).subscribe({
      next: () => { this.successMsg.set('Report deleted.'); this.loadAll(); },
      error: err => this.errorMsg.set(err?.error?.message || 'Delete failed.')
    });
  }

  generateAll() {
    this.loading.set(true); this.genAllResult.set([]);
    this.svc.generateAllReports(this.user).subscribe({
      next: r => { this.loading.set(false); this.genAllResult.set(r); this.successMsg.set(`Reports generated for ${r.length} customer(s).`); },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Generation failed.'); }
    });
  }

  generateSingle() {
    const id = Number(this.genCustomerId());
    if (!id) { this.errorMsg.set('Enter a valid customer ID.'); return; }
    this.loading.set(true); this.genSingleResult.set(null);
    this.svc.generateForCustomer(id, this.user).subscribe({
      next: r => { this.loading.set(false); this.genSingleResult.set(r); this.successMsg.set('Report generated.'); },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Generation failed.'); }
    });
  }

  runFilter() {
    this.loading.set(true); this.filterResult.set([]);
    const f: any = {};
    if (this.filterStart()) f.startDate = this.filterStart();
    if (this.filterEnd())   f.endDate   = this.filterEnd();
    if (this.filterMonth()) f.month = Number(this.filterMonth());
    if (this.filterYear())  f.year  = Number(this.filterYear());
    this.svc.filterReports(f).subscribe({
      next: r => { this.loading.set(false); this.filterResult.set(r); },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Filter failed.'); }
    });
  }

  clearFilter() {
    this.filterStart.set(''); this.filterEnd.set('');
    this.filterMonth.set(''); this.filterYear.set('');
    this.filterResult.set([]);
  }

  loadAudit() {
    this.loading.set(true);
    this.svc.getAuditLogs().subscribe({
      next: r => { this.loading.set(false); this.auditLogs.set(r); },
      error: err => { this.loading.set(false); this.errorMsg.set(err?.error?.message || 'Failed to load audit logs.'); }
    });
  }
}


