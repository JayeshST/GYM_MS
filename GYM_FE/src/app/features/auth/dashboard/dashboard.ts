import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html'
})
export class Dashboard {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly busy = signal(false);
  readonly error = signal('');

  async logout(): Promise<void> {
    if (this.busy()) {
      return;
    }

    this.busy.set(true);
    this.error.set('');

    try {
      await this.auth.logout();
      await this.router.navigateByUrl('/login');
    } catch (error) {
      this.error.set(apiErrorMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}