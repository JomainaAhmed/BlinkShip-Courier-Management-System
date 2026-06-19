import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CourierService } from '../../services/courier.service';
import { NavbarComponent } from '../navbar/navbar.component';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './profile.html'
})
export class ProfileComponent implements OnInit {
  authService = inject(AuthService);
  private courierService = inject(CourierService);
  
  user: User | null = null;
  stats = {
    total: 0,
    delivered: 0,
    pending: 0
  };
  loading = false;

  ngOnInit() {
    this.user = this.authService.getUser();
    if (this.user) {
      this.fetchStats();
    }
  }

  fetchStats() {
    this.loading = true;
    this.courierService.getUserShipments(this.user!.username!).subscribe({
      next: (shipments) => {
        this.stats.total = shipments.length;
        this.stats.delivered = shipments.filter(s => s.status === 'DELIVERED').length;
        this.stats.pending = this.stats.total - this.stats.delivered;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get userInitial(): string {
    return this.user?.username?.charAt(0).toUpperCase() || 'U';
  }
}
