import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-shipment-timeline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="py-10">
      <div class="relative">
        <div class="absolute left-[15px] top-0 bottom-0 w-[2px] bg-white/5"></div>
        <div class="space-y-12">
          <div *ngFor="let step of steps" class="relative flex items-start gap-6 group">
            <div class="w-8 h-8 rounded-full flex items-center justify-center z-10 transition-all duration-500"
                 [ngClass]="step.completed ? 'bg-primary text-black shadow-[0_0_20px_rgba(255,107,0,0.4)]' : 'bg-surface border border-white/10 text-neutral/20'">
              <svg *ngIf="step.completed" xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
              </svg>
              <div *ngIf="!step.completed" class="w-2 h-2 rounded-full bg-current"></div>
            </div>
            
            <div class="flex-1">
              <div class="flex items-center justify-between mb-1">
                <h4 class="text-xs font-bold uppercase tracking-widest" [ngClass]="step.completed ? 'text-white' : 'text-neutral/40'">{{ step.label }}</h4>
                <span *ngIf="step.timestamp" class="text-[9px] font-bold text-primary uppercase tracking-widest">{{ step.timestamp | date:'MMM dd, HH:mm' }}</span>
              </div>
              <p class="text-[10px] font-medium" [ngClass]="step.completed ? 'text-neutral/40' : 'text-neutral/60'">{{ step.desc }}</p>
              <p *ngIf="step.timestamp" class="text-[8px] font-bold text-neutral/20 uppercase tracking-[0.2em] mt-2">{{ getRelativeTime(step.timestamp) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class ShipmentTimelineComponent implements OnInit {
  @Input() shipment: any;
  steps: any[] = [];

  ngOnInit() {
    this.initSteps();
  }

  initSteps() {
    this.steps = [
      {
        label: 'Draft',
        desc: 'Shipment created and saved.',
        completed: !!this.shipment.createdAt,
        timestamp: this.shipment.createdAt
      },
      {
        label: 'Booked',
        desc: 'Shipment has been successfully booked.',
        completed: !!this.shipment.bookedAt,
        timestamp: this.shipment.bookedAt
      },
      {
        label: 'Packed',
        desc: 'Parcel is packed and ready for pickup.',
        completed: !!this.shipment.packedAt,
        timestamp: this.shipment.packedAt
      },
      {
        label: 'Dispatched',
        desc: 'Shipment has left the origin facility.',
        completed: !!this.shipment.dispatchedAt,
        timestamp: this.shipment.dispatchedAt
      },
      {
        label: 'Picked Up',
        desc: 'Courier has picked up the shipment.',
        completed: !!this.shipment.pickedUpAt,
        timestamp: this.shipment.pickedUpAt
      },
      {
        label: 'In Transit',
        desc: 'Shipment is on the way to destination.',
        completed: !!this.shipment.inTransitAt,
        timestamp: this.shipment.inTransitAt
      },
      {
        label: 'Out for Delivery',
        desc: 'Shipment is out for local delivery.',
        completed: !!this.shipment.outForDeliveryAt,
        timestamp: this.shipment.outForDeliveryAt
      },
      {
        label: 'Delivered',
        desc: 'Parcel has been delivered successfully.',
        completed: !!this.shipment.deliveredAt,
        timestamp: this.shipment.deliveredAt
      }
    ];
  }

  getRelativeTime(timestamp: string): string {
    const now = new Date();
    const then = new Date(timestamp);
    const diffInMs = now.getTime() - then.getTime();
    const diffInMins = Math.floor(diffInMs / 60000);

    if (diffInMins < 1) return 'JUST NOW';
    if (diffInMins < 60) return `${diffInMins} MINS AGO`;
    
    const diffInHours = Math.floor(diffInMins / 60);
    if (diffInHours < 24) return `${diffInHours} HOURS AGO`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    return `${diffInDays} DAYS AGO`;
  }
}
