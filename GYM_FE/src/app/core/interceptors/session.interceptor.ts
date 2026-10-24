import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

function isGymApi(url: string): boolean {
  return [
    '/get-csrf-token',
    '/user-login',
    '/get-current-user',
    '/user-logout',
    '/change-password',
    '/register-user',
    '/get-user-list'
  ].includes(url) || url.startsWith('/get-user/');
}

export const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        isGymApi(request.url) &&
        error instanceof HttpErrorResponse &&
        error.status === 401
      ) {
        auth.clearSession();

        if (request.url !== '/user-login') {
          void router.navigateByUrl('/login');
        }
      }

      return throwError(() => error);
    })
  );
};