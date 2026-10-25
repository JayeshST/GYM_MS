import {
  Component,
  computed,
  inject,
  signal
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { apiErrorMessage } from '../../../core/utils/api-error';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  readonly auth = inject(AuthService);

  private readonly router = inject(Router);

  readonly busy = signal(false);
  readonly error = signal('');

  readonly displayRoles = computed(() =>
    (this.auth.user()?.roles ?? []).map(role => ({
      ...role,
      label: role.roleName.trim() || role.roleCode
    }))
  );

  readonly initials = computed(() => {
    const name = this.auth.user()?.fullName.trim() ?? '';

    if (!name) {
      return 'G';
    }

    return name
      .split(/\s+/)
      .slice(0, 2)
      .map(part => Array.from(part)[0] ?? '')
      .join('')
      .toUpperCase();
  });

  readonly firstName = computed(() => {
    const name = this.auth.user()?.fullName.trim() ?? '';
    return name.split(/\s+/)[0] || 'there';
  });

  async logout(): Promise<void> {
    if (this.busy()) {
      return;
    }

    this.busy.set(true);
    this.error.set('');

    try {
      await this.auth.logout();
      await this.router.navigateByUrl('/login');
    } catch (error: unknown) {
      this.error.set(apiErrorMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}