import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { 
    path: '', 
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent) 
  },
  { 
    path: 'login', 
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent) 
  },
  { 
    path: 'signup', 
    loadComponent: () => import('./components/signup/signup.component').then(m => m.SignupComponent) 
  },
  { 
    path: 'dashboard', 
    canActivate: [authGuard],
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent) 
  },
  { 
    path: 'track', 
    loadComponent: () => import('./components/track/track.component').then(m => m.TrackComponent) 
  },
  { 
    path: 'book', 
    canActivate: [authGuard],
    loadComponent: () => import('./components/book-courier/book-courier.component').then(m => m.BookCourierComponent) 
  },
  { 
    path: 'admin', 
    canActivate: [adminGuard],
    loadComponent: () => import('./components/admin/admin.component').then(m => m.AdminComponent) 
  },
  { 
    path: 'profile', 
    canActivate: [authGuard],
    loadComponent: () => import('./components/profile/profile').then(m => m.ProfileComponent) 
  },
  { 
    path: 'my-deliveries', 
    canActivate: [authGuard],
    loadComponent: () => import('./components/my-deliveries/my-deliveries').then(m => m.MyDeliveriesComponent) 
  },
  { 
    path: 'services', 
    loadComponent: () => import('./components/services/services.component').then(m => m.ServicesComponent) 
  },
  { path: '**', redirectTo: '' }


];
