import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="badge" [class]="'badge ' + badgeClass + ' ' + size">{{ status }}</span>`,
  styleUrl: './status-badge.component.css'
})
export class StatusBadgeComponent {
  @Input() status = '';
  @Input() size: 'sm' | 'md' = 'md';

  get badgeClass(): string {
    switch (this.status?.toUpperCase()) {
      case 'SUCCESS': case 'VERIFIED': case 'RESOLVED': case 'CLOSED': case 'LOW':
        return 'green';
      case 'FLAGGED': case 'IN_PROGRESS': case 'REVIEWED': case 'MEDIUM':
        return 'amber';
      case 'BLOCKED': case 'REJECTED': case 'ESCALATED': case 'CRITICAL':
        return 'red';
      case 'HIGH':
        return 'orange';
      case 'NEW': case 'NOT_STARTED':
      default:
        return 'blue';
    }
  }
}

