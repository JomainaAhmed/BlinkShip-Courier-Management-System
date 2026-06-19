import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { User } from '../../models/user.model';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  userData: User = { username: '', email: '', password: '', role: 'USER' };
  loading = false;
  showPassword = false;

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  onSignup() {
    this.loading = true;
    this.authService.signup(this.userData).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.success('Account created! Please sign in.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        const errorMsg = err.error?.message || err.message || 'Registration failed. Please check your connection or try again later.';
        this.toast.error(errorMsg);
        console.error('Signup error', err);
      }
    });
  }
}
