import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard'
  },
  {
    path: 'login',
    title: 'Sign in | Gym MS',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then((component) => component.Login)
  },
 {
  path: 'change-password',
  title: 'Change password | Gym MS',
  canActivate: [authGuard],
  loadComponent: () =>
    import('./features/auth/change-password/changepassword')
      .then((m) => m.ChangePassword)
},
{
  path: 'dashboard',
  title: 'Dashboard | Gym MS',
  canActivate: [authGuard],
  loadComponent: () =>
    import('./features/auth/dashboard/dashboard')
      .then((m) => m.Dashboard)
},
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];