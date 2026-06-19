import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-activity-feed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <div *ngFor="let activity of activities" class="flex items-start gap-4 group">
        <div class="w-2 h-2 rounded-full bg-primary mt-1.5 shadow-[0_0_10px_rgba(255,107,0,0.5)]"></div>
        <div class="flex-1">
          <div class="flex items-center justify-between mb-1">
            <h5 class="text-[10px] font-bold text-white uppercase tracking-widest">{{ activity.action.replace('_', ' ') }}</h5>
            <span class="text-[8px] font-bold text-neutral/40 uppercase tracking-widest">{{ activity.timestamp | date:'MMM dd, HH:mm' }}</span>
          </div>
          <p class="text-[10px] text-neutral/60 font-medium leading-relaxed">{{ activity.details }}</p>
        </div>
      </div>
      <div *ngIf="activities.length === 0" class="text-center py-8">
        <p class="text-[10px] font-bold text-neutral/40 uppercase tracking-[0.4em]">No activity logs found.</p>
      </div>
    </div>
  `,
  styles: []
})
export class ActivityFeedComponent {
  @Input() activities: any[] = [];
}
