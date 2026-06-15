import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  template: `
    <div class="overlay" (click)="cancelled.emit()">
      <div class="dialog" (click)="$event.stopPropagation()">
        <p class="msg">{{ message }}</p>
        <div class="actions">
          <button class="btn-cancel"  (click)="cancelled.emit()">Cancel</button>
          <button class="btn-confirm" (click)="confirmed.emit()">Confirm</button>
        </div>
      </div>
    </div>
  `,
  styleUrl: './confirm-dialog.component.css'
})
export class ConfirmDialogComponent {
  @Input() message = 'Are you sure?';
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();
}

