import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CourierService } from '../../services/courier.service';
import { Shipment } from '../../models/shipment.model';
import { ToastService } from '../../services/toast.service';

import { AdminService } from '../../services/admin.service';
import { User } from '../../models/user.model';
import { NavbarComponent } from '../navbar/navbar.component';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  activeTab: 'dashboard' | 'shipments' | 'users' | 'reports' = 'dashboard';
  shipments: any[] = [];
  users: User[] = [];
  reports: any = null;
  stats: any = {
    total: 0,
    booked: 0,
    inTransit: 0,
    delivered: 0,
    delayed: 0,
    successRate: 0
  };
  filterStatus = '';
  filterDate = '';
  filteredShipments: any[] = [];
  distribution: any = {
    draft: 0,
    booked: 0,
    packed: 0,
    dispatched: 0,
    pickedUp: 0,
    inTransit: 0,
    outForDelivery: 0,
    delivered: 0,
    delayed: 0,
    failed: 0,
    returned: 0
  };
  recentActivities: any[] = [];
  loading = false;

  private courierService = inject(CourierService);
  private adminService = inject(AdminService);
  private notificationService = inject(NotificationService);
  private toast = inject(ToastService);

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.loading = true;
    this.adminService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: () => {
        this.toast.error('Failed to load dashboard statistics.');
        this.loading = false;
      }
    });
  }

  loadShipments() {
    this.loading = true;
    this.adminService.getAllDeliveries().subscribe({
      next: (data) => {
        this.shipments = data;
        this.calculateDistribution();
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.toast.error('Failed to load shipments.');
        this.loading = false;
      }
    });
  }

  calculateDistribution() {
    this.distribution = {
      draft: this.shipments.filter(s => s.status === 'DRAFT').length,
      booked: this.shipments.filter(s => s.status === 'BOOKED').length,
      packed: this.shipments.filter(s => s.status === 'PACKED').length,
      dispatched: this.shipments.filter(s => s.status === 'DISPATCHED').length,
      pickedUp: this.shipments.filter(s => s.status === 'PICKED_UP').length,
      inTransit: this.shipments.filter(s => s.status === 'IN_TRANSIT').length,
      outForDelivery: this.shipments.filter(s => s.status === 'OUT_FOR_DELIVERY').length,
      delivered: this.shipments.filter(s => s.status === 'DELIVERED').length,
      delayed: this.shipments.filter(s => s.status === 'DELAYED').length,
      failed: this.shipments.filter(s => s.status === 'FAILED').length,
      returned: this.shipments.filter(s => s.status === 'RETURNED').length
    };
    this.extractRecentActivities();
  }

  extractRecentActivities() {
    // Collect all activities from all shipments (if available) or just use latest status updates
    this.recentActivities = this.shipments
      .map(s => ({
        id: s.id,
        status: s.status,
        date: s.createdAt,
        user: s.username || 'System'
      }))
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      .slice(0, 5);
  }

  applyFilters() {
    this.filteredShipments = this.shipments.filter(s => {
      const statusMatch = !this.filterStatus || s.status === this.filterStatus;
      const dateMatch = !this.filterDate || s.createdAt?.startsWith(this.filterDate);
      return statusMatch && dateMatch;
    });
  }

  loadUsers() {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: () => {
        this.toast.error('Failed to load users.');
        this.loading = false;
      }
    });
  }

  loadReports() {
    this.loading = true;
    this.adminService.getReports().subscribe({
      next: (data) => {
        this.reports = data;
        this.loading = false;
      },
      error: () => {
        this.toast.error('Failed to load reports.');
        this.loading = false;
      }
    });
  }

  setTab(tab: 'dashboard' | 'shipments' | 'users' | 'reports') {
    this.activeTab = tab;
    if (tab === 'shipments') this.loadShipments();
    if (tab === 'users') this.loadUsers();
    if (tab === 'dashboard') this.loadDashboardData();
    if (tab === 'reports') this.loadReports();
  }

  updateStatus(id: number, status: string) {
    this.courierService.updateShipmentStatus(id.toString(), status).subscribe({
      next: () => {
        this.toast.success(`Shipment #${id} updated to ${status}`);
        this.notificationService.addNotification(`Shipment #${id} status updated to ${status}.`, 'info');
        this.loadShipments();
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Failed to update status.');
      }
    });
  }

  resolveIssue(id: number) {
    this.adminService.resolveIssue(id).subscribe({
      next: (res) => {
        this.toast.success(res);
        this.loadShipments();
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Failed to resolve issue.');
      }
    });
  }

  exportAllShipments() {
    this.adminService.exportAllDeliveries().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'all_shipments.csv';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.toast.error('Failed to export CSV.')
    });
  }

  downloadInvoice(id: number) {
    this.courierService.downloadInvoice(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice_${id}.txt`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.toast.error('Invoice not available.')
    });
  }

  onFileSelected(event: any, deliveryId: number) {
    const file = event.target.files[0];
    if (file) {
      this.courierService.uploadDocument(deliveryId, file).subscribe({
        next: () => this.toast.success('Document uploaded successfully.'),
        error: () => this.toast.error('Upload failed.')
      });
    }
  }
}

