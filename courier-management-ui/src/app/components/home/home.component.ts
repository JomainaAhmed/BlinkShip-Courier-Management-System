import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../shared/footer/footer.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, FooterComponent, FormsModule],
  templateUrl: './home.component.html'
})
export class HomeComponent {
  trackingId: string = '';
  isModalOpen: boolean = false;
  private router = inject(Router);

  openTrackModal() {
    this.isModalOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeTrackModal() {
    this.isModalOpen = false;
    document.body.style.overflow = 'auto';
  }

  scrollToServices() {
    const element = document.getElementById('services');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  faqs = [
    { 
      question: 'How do I track my shipment?', 
      answer: 'You can track your shipment by entering your tracking ID in the search bar on our home page or the tracking page. Real-time updates will show the current status and location of your parcel.',
      isOpen: false 
    },
    { 
      question: 'What is the estimated delivery time?', 
      answer: 'Delivery times vary depending on the service selected. Standard shipments typically take 3-5 business days, while Express shipping can take 24-48 hours.',
      isOpen: false 
    },
    { 
      question: 'How do I book a new delivery?', 
      answer: 'Simply click on the "Book a Delivery" button on the home page. You will need to provide sender and receiver details along with package dimensions to get an instant quote and book your pickup.',
      isOpen: false 
    }
  ];

  toggleFaq(index: number) {
    this.faqs[index].isOpen = !this.faqs[index].isOpen;
  }

  track() {
    if (this.trackingId.trim()) {
      this.router.navigate(['/track'], { queryParams: { id: this.trackingId } });
    }
  }
}
