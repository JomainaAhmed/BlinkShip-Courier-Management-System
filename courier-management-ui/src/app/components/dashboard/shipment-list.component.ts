import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ShipmentTimelineComponent } from '../shared/shipment-timeline.component';
import { ActivityFeedComponent } from './activity-feed.component';

@Component({
  selector: 'app-shipment-list',
  standalone: true,
  imports: [CommonModule, RouterModule, ShipmentTimelineComponent, ActivityFeedComponent],
  template: `
    <div class="space-y-6">
      <!-- Search & Filters -->
      <div class="flex flex-col md:flex-row items-center justify-between gap-6 mb-10">
        <div class="relative w-full md:w-96 group">
          <input type="text" 
                 (input)="onSearch($event)" 
                 placeholder="Search by ID or Receiver Name..." 
                 class="w-full bg-white/[0.03] border border-white/5 rounded-2xl px-12 py-4 text-xs text-white placeholder:text-neutral/30 focus:border-primary/30 focus:bg-white/[0.05] transition-all outline-none">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-neutral/30 group-focus-within:text-primary transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        
        <div class="flex items-center gap-4 w-full md:w-auto">
          <button (click)="onExportCsv()" class="px-6 py-4 bg-white/5 border border-white/5 text-[9px] font-bold text-white uppercase tracking-widest hover:bg-white/10 transition-all rounded-xl flex items-center gap-3">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            Export CSV
          </button>
        </div>
      </div>

      <!-- Shipment Cards -->
      <div *ngFor="let s of filteredShipments" class="blink-card group overflow-hidden border-white/5 hover:border-primary/20 transition-all duration-500">
        <!-- Collapsed Header -->
        <div (click)="toggleExpand(s.id)" class="p-8 cursor-pointer flex flex-wrap items-center justify-between gap-8">
          <div class="flex items-center gap-6">
            <div class="w-12 h-12 rounded-2xl bg-white/5 flex items-center justify-center text-white/20 group-hover:bg-primary/10 group-hover:text-primary transition-all">
               <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <div>
              <div class="flex items-center gap-3 mb-1">
                <span class="text-[10px] font-black text-white tracking-widest uppercase">#{{ s.id }}</span>
                <span class="px-2 py-0.5 rounded-md text-[8px] font-bold tracking-tighter uppercase" 
                      [ngClass]="getStatusClass(s.status)">{{ s.status }}</span>
              </div>
              <p class="text-[10px] font-bold text-neutral/40 uppercase tracking-widest">
                {{ s.sender.city }} <span class="text-primary mx-2">→</span> {{ s.receiver.city }}
              </p>
            </div>
          </div>

          <div class="hidden lg:flex items-center gap-12">
            <div>
              <p class="text-[8px] font-bold text-neutral/40 uppercase tracking-widest mb-1">Weight</p>
              <p class="text-xs font-bold text-white">{{ s.packageDetails.weight | number:'1.2-2' }} KG</p>
            </div>
            <div>
              <p class="text-[8px] font-bold text-neutral/40 uppercase tracking-widest mb-1">Price</p>
              <p class="text-xs font-bold text-white">₹{{ s.packageDetails.price | number:'1.2-2' }}</p>
            </div>
          </div>

          <div class="flex items-center gap-4">
            <button (click)="onDownloadInvoice($event, s.id)" class="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-white/40 hover:bg-primary hover:text-black transition-all" title="Download Invoice">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </button>
            <div class="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-neutral/20 group-hover:text-primary transition-all"
                 [class.rotate-180]="expandedId === s.id">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
        </div>

        <!-- Expanded Content -->
        <div *ngIf="expandedId === s.id" class="border-t border-white/5 bg-white/[0.01] animate-slide-down">
          <div class="grid grid-cols-1 lg:grid-cols-3 divide-y lg:divide-y-0 lg:divide-x divide-white/5">
            <div class="p-8 lg:col-span-2">
              <h4 class="text-[10px] font-bold text-white uppercase tracking-[0.4em] mb-8">Delivery Progress</h4>
              <app-shipment-timeline [shipment]="s"></app-shipment-timeline>
            </div>
            <div class="p-8 bg-black/20">
              <h4 class="text-[10px] font-bold text-white uppercase tracking-[0.4em] mb-8">Recent Activity</h4>
              <app-activity-feed [activities]="s.activities || []"></app-activity-feed>
              <div class="mt-10 pt-10 border-t border-white/5">
                <button [routerLink]="['/track']" [queryParams]="{id: s.id}" class="w-full py-4 bg-primary text-black text-[10px] font-black uppercase tracking-widest rounded-xl hover:shadow-[0_0_20px_rgba(255,107,0,0.3)] transition-all">
                  View Full Tracking Details
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div *ngIf="filteredShipments.length === 0" class="p-20 text-center blink-card border-dashed border-white/10">
        <p class="text-[10px] font-bold text-neutral/40 uppercase tracking-[0.5em]">No shipments matching your criteria.</p>
      </div>
    </div>
  `,
  styles: []
})
export class ShipmentListComponent {
  @Input() shipments: any[] = [];
  @Output() downloadInvoice = new EventEmitter<number>();
  @Output() exportCsv = new EventEmitter<void>();
  @Output() fetchActivities = new EventEmitter<number>();

  filteredShipments: any[] = [];
  expandedId: number | null = null;
  searchTerm: string = '';

  ngOnChanges() {
    this.applyFilters();
  }

  onSearch(event: any) {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilters();
  }

  applyFilters() {
    this.filteredShipments = this.shipments.filter(s => 
      s.id.toString().includes(this.searchTerm) || 
      s.receiver.name.toLowerCase().includes(this.searchTerm)
    );
  }

  toggleExpand(id: number) {
    if (this.expandedId === id) {
      this.expandedId = null;
    } else {
      this.expandedId = id;
      this.fetchActivities.emit(id);
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'DELIVERED': return 'bg-green-500/10 text-green-500';
      case 'FAILED': case 'RETURNED': return 'bg-red-500/10 text-red-500';
      case 'BOOKED': case 'DRAFT': return 'bg-white/5 text-neutral/40';
      default: return 'bg-primary/10 text-primary';
    }
  }

  onDownloadInvoice(event: Event, id: number) {
    event.stopPropagation();
    this.downloadInvoice.emit(id);
  }

  onExportCsv() {
    this.exportCsv.emit();
  }
}
