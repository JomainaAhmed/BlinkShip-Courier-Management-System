import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-shipment-summary',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mb-12 animate-slide-up">
      <div class="blink-card p-6 bg-surface-dim/30 border-white/5 relative group overflow-hidden">
        <div class="absolute -right-4 -bottom-4 opacity-5 group-hover:opacity-10 transition-opacity">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
        </div>
        <p class="text-[9px] font-bold text-neutral/40 uppercase tracking-widest mb-3">Total Shipments</p>
        <p class="text-3xl font-black text-white tracking-tighter">{{ stats.total }}</p>
      </div>

      <div class="blink-card p-6 bg-surface-dim/30 border-white/5 relative group overflow-hidden">
        <div class="absolute -right-4 -bottom-4 opacity-5 group-hover:opacity-10 transition-opacity">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-[9px] font-bold text-neutral/40 uppercase tracking-widest mb-3">In Transit</p>
        <p class="text-3xl font-black text-primary tracking-tighter">{{ stats.inTransit }}</p>
      </div>

      <div class="blink-card p-6 bg-surface-dim/30 border-white/5 relative group overflow-hidden">
        <div class="absolute -right-4 -bottom-4 opacity-5 group-hover:opacity-10 transition-opacity">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-[9px] font-bold text-neutral/40 uppercase tracking-widest mb-3">Delivered</p>
        <p class="text-3xl font-black text-green-500 tracking-tighter">{{ stats.delivered }}</p>
      </div>

      <div class="blink-card p-6 bg-surface-dim/30 border-white/5 relative group overflow-hidden">
        <div class="absolute -right-4 -bottom-4 opacity-5 group-hover:opacity-10 transition-opacity">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-[9px] font-bold text-neutral/40 uppercase tracking-widest mb-3">Total Spent</p>
        <p class="text-3xl font-black text-white tracking-tighter">₹{{ stats.revenue | number:'1.2-2' }}</p>
      </div>
    </div>
  `,
  styles: []
})
export class ShipmentSummaryComponent {
  @Input() stats: any = { total: 0, inTransit: 0, delivered: 0, revenue: 0 };
}
