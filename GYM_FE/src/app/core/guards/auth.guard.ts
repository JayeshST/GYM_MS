import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = async (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  try {
    // Check the backend even after a browser refresh.
    const user = await auth.restoreSession();

    if (
      user.mustChangePassword &&
      !state.url.startsWith('/change-password')
    ) {
      return router.createUrlTree(['/change-password']);
    }

    return true;
  } catch {
    auth.clearSession();
    return router.createUrlTree(['/login']);
  }
};