import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);
  private router = inject(Router);
  
  isDropdownOpen = signal(false);
  isNotificationOpen = signal(false);

  notifications = this.notificationService.getNotifications();
  unreadCount = this.notificationService.unreadCount;

  get user() {
    return this.authService.getUser();
  }

  get userInitial(): string {
    return this.user?.username?.charAt(0).toUpperCase() || 'U';
  }

  toggleDropdown() {
    this.isDropdownOpen.update(v => !v);
    this.isNotificationOpen.set(false);
  }

  toggleNotifications() {
    this.isNotificationOpen.update(v => !v);
    this.isDropdownOpen.set(false);
  }

  closeAll() {
    this.isDropdownOpen.set(false);
    this.isNotificationOpen.set(false);
  }

  markAsRead() {
    this.notificationService.markAllAsRead();
  }

  logout() {
    this.authService.logout();
    this.closeAll();
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }
}
