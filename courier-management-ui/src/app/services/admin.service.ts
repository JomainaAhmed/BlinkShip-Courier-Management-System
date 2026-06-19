import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin`;

  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard`);
  }

  getAllDeliveries(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/deliveries`);
  }

  resolveIssue(id: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/deliveries/${id}/resolve`, {}, { responseType: 'text' });
  }

  updateStatus(id: number, status: string): Observable<string> {
    return this.http.put(`${this.apiUrl}/deliveries/${id}/status?status=${status}`, {}, { responseType: 'text' });
  }


  getReports(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/reports`);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`);
  }

  exportAllDeliveries(): Observable<Blob> {
    // Calling delivery service directly for export
    return this.http.get(`http://localhost:8080/deliveries/export/all`, { responseType: 'blob' });
  }
}
