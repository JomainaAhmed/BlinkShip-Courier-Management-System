import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  loading = false;
  showPassword = false;

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  onLogin() {
    this.loading = true;
    this.authService.login(this.credentials).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.success('Login successful! Welcome back.');
        
        // Redirect based on role from stored user
        const role = this.authService.getRole();
        if (role === 'ADMIN') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.loading = false;
        const errorMsg = err.error?.message || 'Invalid username or password. Please try again.';
        this.toast.error(errorMsg);
      }
    });
  }

}
