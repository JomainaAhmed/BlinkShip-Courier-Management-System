import { Injectable, signal } from '@angular/core';

export interface Notification {
  id: number;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  timestamp: Date;
  read: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = signal<Notification[]>([]);
  unreadCount = signal(0);

  getNotifications() {
    return this.notifications.asReadonly();
  }

  addNotification(message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') {
    const newNotification: Notification = {
      id: Date.now(),
      message,
      type,
      timestamp: new Date(),
      read: false
    };
    
    this.notifications.update(prev => [newNotification, ...prev]);
    this.unreadCount.update(count => count + 1);
  }

  markAllAsRead() {
    this.notifications.update(prev => prev.map(n => ({ ...n, read: true })));
    this.unreadCount.set(0);
  }

  clearAll() {
    this.notifications.set([]);
    this.unreadCount.set(0);
  }
}
