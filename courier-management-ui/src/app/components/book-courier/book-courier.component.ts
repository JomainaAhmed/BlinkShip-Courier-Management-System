import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CourierService } from '../../services/courier.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { NavbarComponent } from '../navbar/navbar.component';
import { LabelPrintUtil } from '../../core/utils/label-print.util';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-book-courier',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent],
  templateUrl: './book-courier.component.html',
  styleUrl: './book-courier.component.css'
})
export class BookCourierComponent implements OnInit {
  bookingForm!: FormGroup;
  
  loading = false;
  estimatedPrice: number | null = null;
  bookedShipment: any | null = null;

  private fb = inject(FormBuilder);


  private authService = inject(AuthService);
  private courierService = inject(CourierService);
  private notificationService = inject(NotificationService);
  private toast = inject(ToastService);
  private router = inject(Router);

  ngOnInit() {
    this.initForm();
    const user = this.authService.getUser();
    if (user) {
      this.bookingForm.patchValue({ senderName: user.username });
    }
    
    // Subscribe to value changes for price estimation
    this.bookingForm.valueChanges.subscribe(() => {
      this.updatePrice();
    });

    this.updatePrice();
  }

  private initForm() {
    this.bookingForm = this.fb.group({
      senderName: ['', [Validators.required, Validators.minLength(3)]],
      senderPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      senderAddress: ['', [Validators.required, Validators.minLength(10)]],
      senderCity: ['', [Validators.required]],
      senderState: ['', [Validators.required]],
      senderPincode: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]],
      
      receiverName: ['', [Validators.required, Validators.minLength(3)]],
      receiverPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      receiverAddress: ['', [Validators.required, Validators.minLength(10)]],
      receiverCity: ['', [Validators.required]],
      receiverState: ['', [Validators.required]],
      receiverPincode: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]],
      
      description: ['', [Validators.required]],
      weight: [1, [Validators.required, Validators.min(0.1)]],
      length: [1, [Validators.required, Validators.min(1)]],
      width: [1, [Validators.required, Validators.min(1)]],
      height: [1, [Validators.required, Validators.min(1)]]
    });
  }

  updatePrice() {
    const val = this.bookingForm.value;
    const details = {
      weight: val.weight,
      length: val.length,
      width: val.width,
      height: val.height
    };
    
    if (details.weight > 0 && this.bookingForm.valid) {
      this.courierService.calculatePrice(details).subscribe({
        next: (price) => this.estimatedPrice = price,
        error: () => {
          // Frontend Fallback Calculation
          const volumetricWeight = (details.length * details.width * details.height) / 5000.0;
          const chargeableWeight = Math.max(details.weight, volumetricWeight);
          this.estimatedPrice = chargeableWeight * 300;
        }
      });
    } else {
      this.estimatedPrice = 0;
    }
  }



  onSubmit() {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const val = this.bookingForm.value;
    
    const shipmentData: any = {
      sender: {
        name: val.senderName,
        phone: val.senderPhone,
        addressLine: val.senderAddress,
        city: val.senderCity,
        state: val.senderState,
        pincode: val.senderPincode
      },
      receiver: {
        name: val.receiverName,
        phone: val.receiverPhone,
        addressLine: val.receiverAddress,
        city: val.receiverCity,
        state: val.receiverState,
        pincode: val.receiverPincode
      },
      packageDetails: {
        description: val.description,
        weight: val.weight,
        length: val.length,
        width: val.width,
        height: val.height
      }
    };

    this.courierService.createShipment(shipmentData).subscribe({
      next: (res) => {
        this.loading = false;
        this.bookedShipment = res;
        this.toast.success(`Shipment #${res.id} booked successfully!`);
        this.notificationService.addNotification(`Shipment #${res.id} has been created.`, 'success');
      },
      error: (err) => {
        this.loading = false;
        const errorMsg = err.error?.message || 'Failed to book shipment. Please try again.';
        this.toast.error(errorMsg);
      }
    });
  }
  printLabel() {
    if (this.bookedShipment) {
      LabelPrintUtil.printLabel(this.bookedShipment);
    }
  }

  resetForm() {
    this.bookedShipment = null;
    this.router.navigate(['/dashboard']);
  }
}

