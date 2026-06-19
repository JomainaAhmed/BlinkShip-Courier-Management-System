import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { CourierService } from '../../services/courier.service';
import { AuthService } from '../../services/auth.service';
import { Shipment } from '../../models/shipment.model';

import { NavbarComponent } from '../navbar/navbar.component';
import { ShipmentSummaryComponent } from './shipment-summary.component';
import { ShipmentListComponent } from './shipment-list.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, ShipmentSummaryComponent, ShipmentListComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  shipments: Shipment[] = [];
  loading = true;
  user = inject(AuthService).getUser();

  private courierService = inject(CourierService);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit() {
    this.loadShipments();
  }

  loadShipments() {
    this.loading = true;
    const username = this.user?.username;
    
    if (!username) {
      this.loading = false;
      return;
    }

    this.courierService.getUserShipments(username).subscribe({
      next: (data) => {
        this.shipments = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading shipments', err);
        this.loading = false;
      }
    });
  }


  getStats() {
    const revenue = this.shipments.reduce((acc, s) => acc + (s.packageDetails?.price || 0), 0);
    return {
      total: this.shipments.length,
      delivered: this.shipments.filter(s => s.status === 'DELIVERED').length,
      inTransit: this.shipments.filter(s => ['PICKED_UP', 'PACKED', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'].includes(s.status || '')).length,
      revenue: revenue
    };
  }

  fetchActivities(id: number) {
    const shipment = this.shipments.find(s => s.id === id);
    if (shipment && !shipment.activities) {
      this.courierService.getShipmentActivities(id).subscribe({
        next: (activities) => shipment.activities = activities,
        error: (err) => console.error('Error fetching activities', err)
      });
    }
  }

  downloadInvoice(id: number) {
    this.courierService.downloadInvoice(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice_${id}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error downloading invoice', err)
    });
  }

  exportCsv() {
    const username = this.user?.username;
    if (!username) return;
    
    this.courierService.exportCsv(username).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `shipments_${username}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error exporting CSV', err)
    });
  }

  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
