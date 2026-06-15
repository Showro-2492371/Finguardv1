import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FraudCheckResponse } from '../../models/transaction.models';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { RiskProgressBarComponent } from '../../shared/components/risk-progress-bar/risk-progress-bar.component';
import { parseBackendDate } from '../../core/utils/date.helper';

@Component({
  selector: 'app-fraud-result',
  standalone: true,
  imports: [CommonModule, DatePipe, StatusBadgeComponent, RiskProgressBarComponent],
  templateUrl: './fraud-result.component.html',
  styleUrl: './fraud-result.component.css'
})
export class FraudResultComponent implements OnChanges {
  @Input() result: FraudCheckResponse | null = null;

  fraudReasons: string[] = [];
  parsedDate: Date | null = null;

  ngOnChanges() {
    if (!this.result) return;
    this.fraudReasons = (this.result.fraudReason || '')
      .split(', ')
      .filter(r => r.trim() && r !== 'No fraud indicators detected');
    this.parsedDate = parseBackendDate(this.result.createdAt);
  }
}

