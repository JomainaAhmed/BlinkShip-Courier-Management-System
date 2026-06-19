import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CourierService } from '../../services/courier.service';
import { AuthService } from '../../services/auth.service';
import { Shipment, TrackingStep } from '../../models/shipment.model';
import { ToastService } from '../../services/toast.service';
import { forkJoin } from 'rxjs';

import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-track',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './track.component.html',
  styleUrl: './track.component.css'
})
export class TrackComponent implements OnInit {
  trackingNumber = '';
  shipment: Shipment | null = null;
  steps: TrackingStep[] = [];
  loading = false;

  private courierService = inject(CourierService);
  private toast = inject(ToastService);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  isAdmin = false;

  ngOnInit() {
    this.isAdmin = this.authService.getRole() === 'ADMIN';
    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.trackingNumber = params['id'];
        this.onTrack();
      }
    });
  }

  allPossibleSteps = [
    { status: 'DRAFT', label: 'Draft', icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' },
    { status: 'BOOKED', label: 'Booked', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
    { status: 'PACKED', label: 'Packed', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4' },
    { status: 'DISPATCHED', label: 'Dispatched', icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { status: 'PICKED_UP', label: 'Picked Up', icon: 'M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4' },
    { status: 'IN_TRANSIT', label: 'In Transit', icon: 'M13 10V3L4 14h7v7l9-11h-7z' },
    { status: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4' },
    { status: 'DELIVERED', label: 'Delivered', icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z' }
  ];

  exceptionStates = ['DELAYED', 'FAILED', 'RETURNED'];


  getProgress(): number {
    if (!this.shipment) return 0;
    if (this.isException()) return 100; // Show full bar but red in HTML
    const currentIndex = this.allPossibleSteps.findIndex(s => s.status === this.shipment?.status);
    if (currentIndex === -1) return 0;
    return ((currentIndex + 1) / this.allPossibleSteps.length) * 100;
  }

  isStepCompleted(status: string): boolean {
    if (!this.shipment) return false;
    if (this.isException()) return true; // If failed/delayed, all previous steps are technically reached
    const currentIndex = this.allPossibleSteps.findIndex(s => s.status === this.shipment?.status);
    const stepIndex = this.allPossibleSteps.findIndex(s => s.status === status);
    return stepIndex <= currentIndex;
  }

  isException(): boolean {
    return this.exceptionStates.includes(this.shipment?.status || '');
  }

  onTrack() {
    if (!this.trackingNumber) return;
    
    this.loading = true;
    this.shipment = null;
    this.steps = [];
    
    forkJoin({
      shipment: this.courierService.getShipmentById(this.trackingNumber),
      steps: this.courierService.trackCourier(this.trackingNumber)
    }).subscribe({
      next: (res) => {
        this.shipment = this.normalizeDates(res.shipment);
        this.steps = res.steps.map(step => ({
          ...step,
          timestamp: this.ensureUtc(step.timestamp)
        }));
        this.loading = false;
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Shipment not found.');
        this.loading = false;
      }
    });
  }

  handleFileUpload(event: any) {
    const file = event.target.files[0];
    const shipment = this.shipment;
    if (file && shipment && shipment.id) {
      const shipmentId = shipment.id;
      this.loading = true;
      this.courierService.uploadDocument(shipmentId, file).subscribe({
        next: (msg) => {
          this.toast.success(msg);
          this.loading = false;
        },
        error: (err) => {
          this.toast.error(err.error || 'Upload failed');
          this.loading = false;
        }
      });
    }
  }

  downloadDocument() {
    const shipment = this.shipment;
    if (shipment && shipment.id) {
      const shipmentId = shipment.id;
      this.courierService.downloadInvoice(shipmentId).subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `invoice_${shipmentId}.pdf`;
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: () => this.toast.error('Download failed')
      });
    }
  }

  onDownload(trackingId: number) {
    // Assuming we want to download a document with a generic name if not specified
    this.courierService.downloadDocument(trackingId, 'document.pdf').subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `tracking_doc_${trackingId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.toast.error('No document found for this step.')
    });
  }

  private normalizeDates(s: Shipment): Shipment {
    return {
      ...s,
      createdAt: this.ensureUtc(s.createdAt),
      updatedAt: this.ensureUtc(s.updatedAt),
      packedAt: this.ensureUtc(s.packedAt),
      dispatchedAt: this.ensureUtc(s.dispatchedAt),
      inTransitAt: this.ensureUtc(s.inTransitAt),
      deliveredAt: this.ensureUtc(s.deliveredAt)
    };
  }

  private ensureUtc(dateStr?: string): string | undefined {
    if (!dateStr) return dateStr;
    if (dateStr.endsWith('Z')) return dateStr;
    // If it's a numeric array (some Jackson configs), convert to string or handle
    if (Array.isArray(dateStr)) {
       // handle array [2026, 5, 4, 10, 28] if needed
       return dateStr.toString();
    }
    return dateStr + 'Z';
  }
}

