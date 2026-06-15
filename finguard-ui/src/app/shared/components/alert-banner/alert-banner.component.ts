import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-alert-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="banner" [class]="type" role="alert">
      <span class="icon">{{ icon }}</span>
      <span class="msg">{{ message }}</span>
      <button class="close" (click)="dismissed.emit()" aria-label="Dismiss">×</button>
    </div>
  `,
  styleUrl: './alert-banner.component.css'
})
export class AlertBannerComponent {
  @Input() type: 'success' | 'error' | 'info' | 'warning' = 'info';
  @Input() message = '';
  @Output() dismissed = new EventEmitter<void>();
  get icon() {
    return { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' }[this.type];
  }
}

