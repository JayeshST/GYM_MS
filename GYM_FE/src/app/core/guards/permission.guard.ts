import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../services/auth.service';
import { AccessService } from '../services/access.service';

export function permissionGuard(
  requiredPermissions: readonly string[]
): CanActivateFn {
  return async () => {
    const auth = inject(AuthService);
    const access = inject(AccessService);
    const router = inject(Router);

    try {
      const user = await auth.restoreSession();

      if (user.mustChangePassword) {
        return router.createUrlTree(['/change-password']);
      }

      await access.load();

      if (access.hasAll(requiredPermissions)) {
        return true;
      }

      return router.createUrlTree(['/access-denied']);
    } catch (error: unknown) {
      access.clear();

      if (
        error instanceof HttpErrorResponse &&
        error.status === 401
      ) {
        auth.clearSession();
        return router.createUrlTree(['/login']);
      }

      // Failed lookup must not grant access.
      return router.createUrlTree(['/access-denied'], {
        queryParams: { reason: 'verification' }
      });
    }
  };
}