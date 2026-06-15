import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-risk-progress-bar',
  standalone: true,
  template: `
    <div class="bar-wrap">
      <div class="bar-track">
        <div class="bar-fill" [style.width.%]="width" [style.background]="color"></div>
      </div>
      <span class="bar-label" [style.color]="color">{{ score }} / {{ score > 100 ? score : '100+' }}</span>
    </div>
  `,
  styleUrl: './risk-progress-bar.component.css'
})
export class RiskProgressBarComponent {
  @Input() score = 0;
  get width() { return Math.min(this.score, 100); }
  get color() {
    if (this.score >= 60) return '#dc3545';
    if (this.score >= 30) return '#ffc107';
    return '#28a745';
  }
}

