import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { RiskProgressBarComponent } from '../../../shared/components/risk-progress-bar/risk-progress-bar.component';
import { RiskAlertService } from '../../../services/risk-alert.service';
import { RiskAlertResponse, RiskAlertStatus } from '../../../models/risk-alert.models';
import { parseBackendDate } from '../../../core/utils/date.helper';

@Component({
  selector: 'app-risk-alerts-page',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, NavbarComponent, AlertBannerComponent,
    LoadingSpinnerComponent, StatusBadgeComponent, RiskProgressBarComponent],
  templateUrl: './risk-alerts-page.component.html',
  styleUrl: './risk-alerts-page.component.css'
})
export class RiskAlertsPageComponent implements OnInit {
  private svc = inject(RiskAlertService);

  loading = signal(false);
  resolvingId = signal<number | null>(null);
  successMsg = signal('');
  errorMsg = signal('');
  alerts = signal<RiskAlertResponse[]>([]);
  expandedId = signal<number | null>(null);
  statusFilter: RiskAlertStatus | '' = '';
  parseDate = parseBackendDate;

  statuses: Array<RiskAlertStatus | ''> = ['', 'NEW', 'ESCALATED', 'RESOLVED'];

  ngOnInit() { this.load(); }

  onStatusChange() { this.load(); }

  load() {
    this.loading.set(true); this.errorMsg.set('');
    this.svc.getAlerts(this.statusFilter || undefined).subscribe({
      next: list => {
        this.loading.set(false);
        this.alerts.set([...list].sort((a,b) => b.riskScore - a.riskScore));
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || 'Failed to load alerts.');
      }
    });
  }

  canResolve(status: RiskAlertStatus): boolean {
    return status === 'ESCALATED';
  }

  resolveAlert(alertId: number) {
    this.resolvingId.set(alertId);
    this.errorMsg.set('');
    this.successMsg.set('');

    this.svc.updateAlertStatus(alertId, 'RESOLVED').subscribe({
      next: () => {
        this.resolvingId.set(null);
        this.successMsg.set(`Alert ${alertId} resolved successfully.`);
        this.load();
      },
      error: err => {
        this.resolvingId.set(null);
        this.errorMsg.set(err?.error?.message || 'Failed to resolve alert.');
      }
    });
  }

  toggleExpand(id: number) {
    this.expandedId.update(v => v === id ? null : id);
  }
}






