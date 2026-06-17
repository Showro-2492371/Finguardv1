import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { AlertBannerComponent } from '../../../shared/components/alert-banner/alert-banner.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { AnalyticsService } from '../../../services/analytics.service';
import { FraudAnalyticsDTO } from '../../../models/analytics.models';
import { parseBackendDate } from '../../../core/utils/date.helper';

@Component({
  selector: 'app-analytics-page',
  standalone: true,
  imports: [CommonModule, DatePipe, DecimalPipe, NavbarComponent, AlertBannerComponent, LoadingSpinnerComponent],
  templateUrl: './analytics-page.component.html',
  styleUrl: './analytics-page.component.css'
})
export class AnalyticsPageComponent implements OnInit {
  private svc = inject(AnalyticsService);
  parseDate = parseBackendDate;

  loading   = signal(false);
  genLoading = signal(false);
  errorMsg  = signal('');
  successMsg = signal('');

  latest  = signal<FraudAnalyticsDTO | null>(null);
  history = signal<FraudAnalyticsDTO[]>([]);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    this.svc.getHistory().subscribe({
      next: list => {
        this.loading.set(false);
        this.history.set(list);
        if (list.length > 0) this.latest.set(list[0]);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || 'Failed to load analytics data.');
      }
    });
  }

  generateSnapshot() {
    this.genLoading.set(true);
    this.successMsg.set('');
    this.svc.generateSnapshot().subscribe({
      next: snap => {
        this.genLoading.set(false);
        this.latest.set(snap);
        this.successMsg.set('Analytics snapshot generated successfully.');
        this.loadData();
      },
      error: err => {
        this.genLoading.set(false);
        this.errorMsg.set(err?.error?.message || 'Snapshot generation failed.');
      }
    });
  }

  /** Determines the severity CSS class for a given fraud rate */
  fraudRateClass(rate: number): string {
    if (rate >= 50) return 'badge-critical';
    if (rate >= 25) return 'badge-high';
    if (rate >= 10) return 'badge-medium';
    return 'badge-low';
  }

  /** Clamp bar width to [0,100] for progress bars */
  clamp(v: number): number {
    return Math.min(100, Math.max(0, v));
  }
}

