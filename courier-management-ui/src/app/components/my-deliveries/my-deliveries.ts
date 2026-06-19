import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CourierService } from '../../services/courier.service';
import { AuthService } from '../../services/auth.service';
import { Shipment } from '../../models/shipment.model';
import { NavbarComponent } from '../navbar/navbar.component';
import { ShipmentListComponent } from '../dashboard/shipment-list.component';

@Component({
  selector: 'app-my-deliveries',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, NavbarComponent, ShipmentListComponent],
  templateUrl: './my-deliveries.html'
})
export class MyDeliveriesComponent implements OnInit {
  shipments: Shipment[] = [];
  filteredShipments: Shipment[] = [];
  loading = true;
  searchTerm = '';
  statusFilter = '';
  
  isModalOpen = false;
  selectedShipment: Shipment | null = null;
  
  user = inject(AuthService).getUser();
  private courierService = inject(CourierService);

  ngOnInit() {
    this.loadShipments();
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

  loadShipments() {
    this.loading = true;
    if (this.user?.username) {
      this.courierService.getUserShipments(this.user.username).subscribe({
        next: (data) => {
          this.shipments = data;
          this.applyFilters();
          this.loading = false;
        },
        error: () => this.loading = false
      });
    }
  }

  applyFilters() {
    this.filteredShipments = this.shipments.filter(s => {
      const matchesSearch = !this.searchTerm || 
        s.id?.toString().includes(this.searchTerm) ||
        s.receiver.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        s.receiver.city.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesStatus = !this.statusFilter || s.status === this.statusFilter;
      
      return matchesSearch && matchesStatus;
    });
  }
}
