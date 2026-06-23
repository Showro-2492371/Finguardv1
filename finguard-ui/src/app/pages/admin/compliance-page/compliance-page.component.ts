import { Component, inject, signal, PLATFORM_ID, OnInit } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { finalize, map, switchMap } from 'rxjs/operators';
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
  rawCsv: string;
  headers: string[];
  rows: string[][];
}

interface FilterState {
  start: string;
  end: string;
  month: string;
  year: string;
}

@Component({
  selector: 'app-compliance-page',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, NavbarComponent, AlertBannerComponent,
    LoadingSpinnerComponent, ConfirmDialogComponent],
  templateUrl: './compliance-page.component.html',
  styleUrl: './compliance-page.component.css'
})
export class CompliancePageComponent implements OnInit {
  private svc = inject(ComplianceService);
  private auth = inject(AuthService);
  private platformId = inject(PLATFORM_ID);
  parseDate = parseBackendDate;

  activeTab = signal<'all'|'generate'|'filter'|'audit'>('all');
  loading = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  allReports = signal<ComplianceReportDTO[]>([]);
  deleteTarget = signal<ComplianceReportDTO | null>(null);
  csvPreview = signal<CsvPreview | null>(null);
  csvLoading = signal(false);

  genAllResult = signal<ComplianceReportDTO[]>([]);
  genCustomerId = signal('');
  genSingleResult = signal<ComplianceReportDTO | null>(null);

  filters = signal<FilterState>({ start: '', end: '', month: '', year: '' });
  filterResult = signal<ComplianceReportDTO[]>([]);

  auditLogs = signal<AuditTrail[]>([]);

  get user() { return this.auth.getUsername(); }

  ngOnInit(): void {
    this.loadAll();
  }

  switchTab(tab: 'all'|'generate'|'filter'|'audit') {
    this.activeTab.set(tab);
    this.clearMessages();
    this.csvPreview.set(null);
    if (tab === 'all')   this.loadAll();
    if (tab === 'audit') this.loadAudit();
  }

  loadAll() {
    this.runRequest(this.svc.getAllReports(), this.allReports, 'Failed to load reports.');
  }

  exportCsv(customerId: number) {
    this.csvPreview.set(null);
    this.csvLoading.set(true);
    this.clearMessages();

    this.svc.exportCsv(customerId, this.user).pipe(
      map(exportResponse => {
        const filePath = this.extractCsvPath(exportResponse);
        if (!filePath) {
          throw new Error('CSV was generated, but file path could not be extracted from backend response.');
        }
        return filePath;
      }),
      switchMap(filePath =>
        this.svc.fetchGeneratedCsv(customerId).pipe(
          map(rawCsv => ({ filePath, rawCsv }))
        )
      ),
      finalize(() => this.csvLoading.set(false))
    ).subscribe({
      next: ({ filePath, rawCsv }) => {
        const parsed = this.parseCsv(rawCsv);
        this.csvPreview.set({ customerId, filePath, rawCsv, headers: parsed.headers, rows: parsed.rows });
        this.successMsg.set(`✓ CSV generated and loaded from backend: ${filePath}`);
      },
      error: err => {
        this.errorMsg.set(err?.error?.message || err?.message || 'Backend CSV export failed.');
      }
    });
  }

  downloadCsvLocally(preview: CsvPreview) {
    if (!isPlatformBrowser(this.platformId)) return;
    const blob = new Blob([preview.rawCsv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `report_customer_${preview.customerId}.csv`;
    a.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  closeCsvPreview() {
    this.csvPreview.set(null);
  }

  private extractCsvPath(exportResponse: string): string | null {
    const trimmed = (exportResponse ?? '').trim();
    if (!trimmed) return null;

    // Try to extract path from response like "CSV generated at: reports/report_customer_X.csv"
    const match = trimmed.match(/(reports[\\/][^\s]+\.csv)/i);
    if (match) return match[1];

    // If response is already a path, use it directly
    if (/\.csv$/i.test(trimmed)) {
      return trimmed;
    }

    return null;
  }

  private parseCsv(rawCsv: string): { headers: string[]; rows: string[][] } {
    const lines = rawCsv
      .split(/\r?\n/)
      .map(l => l.trim())
      .filter(l => l.length > 0);

    if (lines.length === 0) {
      return { headers: [], rows: [] };
    }

    const headers = this.parseCsvLine(lines[0]);
    const rows = lines.slice(1).map(line => this.parseCsvLine(line));
    return { headers, rows };
  }

  private parseCsvLine(line: string): string[] {
    const result: string[] = [];
    let current = '';
    let inQuotes = false;

    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      const next = line[i + 1];

      if (ch === '"') {
        if (inQuotes && next === '"') {
          current += '"';
          i++;
        } else {
          inQuotes = !inQuotes;
        }
        continue;
      }

      if (ch === ',' && !inQuotes) {
        result.push(current.trim());
        current = '';
        continue;
      }

      current += ch;
    }

    result.push(current.trim());
    return result;
  }

  hasRiskMismatch(report: ComplianceReportDTO): boolean {
    return report.fraudCases > 0 && report.riskScore <= 0;
  }

  askDelete(report: ComplianceReportDTO) { this.deleteTarget.set(report); }
  cancelDelete()       { this.deleteTarget.set(null); }
  confirmDelete() {
    const report = this.deleteTarget();
    if (!report) return;
    this.deleteTarget.set(null);
    this.runRequest(this.svc.deleteReport(report.reportId, this.user), undefined, 'Delete failed.', () => {
      this.successMsg.set('Report deleted.');
      this.loadAll();
    });
  }

  generateAll() {
    this.genAllResult.set([]);
    this.runRequest(this.svc.generateAllReports(this.user), this.genAllResult, 'Generation failed.', r => {
      this.successMsg.set(`Reports generated for ${r.length} customer(s).`);
    });
  }

  generateSingle() {
    const id = Number(this.genCustomerId());
    if (!id) { this.errorMsg.set('Enter a valid customer ID.'); return; }
    this.genSingleResult.set(null);
    this.runRequest(this.svc.generateForCustomer(id, this.user), this.genSingleResult, 'Generation failed.', () => {
      this.successMsg.set('Report generated.');
    });
  }

  runFilter() {
    this.filterResult.set([]);
    const { start, end, month, year } = this.filters();
    const f: any = {};
    if (start) f.startDate = start;
    if (end)   f.endDate   = end;
    if (month) f.month = Number(month);
    if (year)  f.year  = Number(year);
    this.runRequest(this.svc.filterReports(f), this.filterResult, 'Filter failed.');
  }

  clearFilter() {
    this.filters.set({ start: '', end: '', month: '', year: '' });
    this.filterResult.set([]);
  }

  loadAudit() {
    this.runRequest(this.svc.getAuditLogs(), this.auditLogs, 'Failed to load audit logs.');
  }

  private clearMessages() {
    this.successMsg.set('');
    this.errorMsg.set('');
  }

  setFilter<K extends keyof FilterState>(key: K, value: string) {
    this.filters.update(current => ({ ...current, [key]: value }));
  }

  private runRequest<T>(
    request$: Observable<T>,
    target?: { set(value: T): void },
    fallbackError = 'Operation failed.',
    afterSuccess?: (value: T) => void
  ) {
    this.loading.set(true);
    request$.pipe(finalize(() => this.loading.set(false))).subscribe({
      next: value => {
        target?.set(value);
        afterSuccess?.(value);
      },
      error: err => {
        this.errorMsg.set(err?.error?.message || fallbackError);
      }
    });
  }
}


