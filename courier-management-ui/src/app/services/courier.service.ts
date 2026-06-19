import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { Shipment, TrackingInfo } from '../models/shipment.model';

@Injectable({
  providedIn: 'root'
})
export class CourierService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  createShipment(data: Shipment): Observable<any> {
    // Backend path: /deliveries/create
    return this.http.post<any>(`${this.apiUrl}/deliveries/create`, data);
  }



  calculatePrice(details: any): Observable<number> {
    return this.http.post<number>(`${this.apiUrl}/deliveries/calculate`, details);
  }





  trackCourier(deliveryId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tracking/${deliveryId}`).pipe(
      map(list => list.map(step => ({
        ...step,
        timestamp: step.timestamp && !step.timestamp.endsWith('Z') ? step.timestamp + 'Z' : step.timestamp
      })))
    );
  }

  updateShipmentStatus(id: string, status: string): Observable<any> {
    // Backend path: /deliveries/{id}/status?status=...
    const params = new HttpParams().set('status', status);
    return this.http.put<any>(`${this.apiUrl}/deliveries/${id}/status`, {}, { params });
  }

  getShipmentActivities(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/deliveries/${id}/activities`).pipe(
      map(list => list.map(act => ({
        ...act,
        timestamp: act.timestamp && !act.timestamp.endsWith('Z') ? act.timestamp + 'Z' : act.timestamp
      })))
    );
  }

  downloadInvoice(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/deliveries/invoice/${id}`, { responseType: 'blob' });
  }

  exportCsv(username: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/deliveries/export/${username}`, { responseType: 'blob' });
  }

  uploadDocument(deliveryId: number, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/deliveries/documents/${deliveryId}/upload`, formData, { responseType: 'text' });
  }

  downloadDocument(deliveryId: number, fileName: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/deliveries/documents/${deliveryId}/${fileName}`, { responseType: 'blob' });
  }

  // Helper to normalize dates to UTC if 'Z' is missing
  private normalizeShipment(s: any): any {
    if (!s) return s;
    const normalize = (dateStr: any) => {
      if (!dateStr || typeof dateStr !== 'string' || dateStr.endsWith('Z')) return dateStr;
      return dateStr + 'Z';
    };

    return {
      ...s,
      createdAt: normalize(s.createdAt),
      updatedAt: normalize(s.updatedAt),
      packedAt: normalize(s.packedAt),
      dispatchedAt: normalize(s.dispatchedAt),
      inTransitAt: normalize(s.inTransitAt),
      deliveredAt: normalize(s.deliveredAt)
    };
  }

  getUserShipments(username: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/deliveries/user/${username}`).pipe(
      map(list => list.map(s => this.normalizeShipment(s)))
    );
  }

  getAllShipments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/deliveries/all`).pipe(
      map(list => list.map(s => this.normalizeShipment(s)))
    );
  }

  getShipmentById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/deliveries/${id}`).pipe(
      map(s => this.normalizeShipment(s))
    );
  }
}
