import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { ComplianceService } from '../../../services/compliance.service';
import { RiskAlertService } from '../../../services/risk-alert.service';
import { AuthService } from '../../../core/services/auth.service';
import { ComplianceSummary } from '../../../models/compliance.models';
import { RiskAlertResponse } from '../../../models/risk-alert.models';
import { parseBackendDate } from '../../../core/utils/date.helper';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, NavbarComponent, AlertBannerComponent, LoadingSpinnerComponent, StatusBadgeComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private compliance = inject(ComplianceService);
  private riskAlert = inject(RiskAlertService);
  private auth = inject(AuthService);

  loading = signal(true);
  errorMsg = signal('');
  genLoading = signal(false);
  genSuccess = signal('');

  summary = signal<ComplianceSummary | null>(null);
  recentAlerts = signal<RiskAlertResponse[]>([]);

  // Count-up animated values
  countCustomers = signal(0);
  countFraud = signal(0);
  countRisk = signal(0);

  parseDate = parseBackendDate;

  ngOnInit() {
    forkJoin({
      summary: this.compliance.getSummary(),
      alerts:  this.riskAlert.getAlerts()
    }).subscribe({
      next: ({ summary, alerts }) => {
        this.loading.set(false);
        this.summary.set(summary);
        this.recentAlerts.set([...alerts].sort((a,b) => b.riskScore - a.riskScore).slice(0,5));
        this.animateCount(this.countCustomers, summary.totalCustomers);
        this.animateCount(this.countFraud, summary.totalFraudCases);
        this.animateCount(this.countRisk, Math.round(summary.totalRiskScore));
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || err?.message || 'Failed to load dashboard data.');
      }
    });
  }

  private animateCount(sig: ReturnType<typeof signal<number>>, target: number) {
    const step = Math.max(1, Math.ceil(target / 60));
    const interval = setInterval(() => {
      sig.update(v => {
        const next = v + step;
        if (next >= target) { clearInterval(interval); return target; }
        return next;
      });
    }, 16);
  }

  generateAll() {
    this.genLoading.set(true); this.genSuccess.set('');
    this.compliance.generateAllReports(this.auth.getUsername()).subscribe({
      next: list => {
        this.genLoading.set(false);
        this.genSuccess.set(`Reports generated for ${list.length} customer(s).`);
      },
      error: err => {
        this.genLoading.set(false);
        this.errorMsg.set(err?.error?.message || 'Failed to generate reports.');
      }
    });
  }
}

