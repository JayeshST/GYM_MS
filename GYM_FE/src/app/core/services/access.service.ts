import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { ApiResponse, CurrentUser } from '../models/auth.models';
import {
  GYM_PERMISSIONS,
  MyAccess
} from '../models/access.models';
import { AuthService } from './auth.service';

interface AccessSnapshot {
  user: CurrentUser;
  permissions: ReadonlySet<string>;
}

@Injectable({
  providedIn: 'root'
})
export class AccessService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  private readonly snapshot = signal<AccessSnapshot | null>(null);

  private requestVersion = 0;

  readonly loading = signal(false);
  readonly error = signal('');

  readonly ready = computed(() => {
    const snapshot = this.snapshot();
    const user = this.auth.user();

    return user !== null && snapshot?.user === user;
  });

  readonly canViewUsers = computed(() =>
    this.has(GYM_PERMISSIONS.VIEW_USERS)
  );

  readonly canCreateUsers = computed(() =>
    this.hasAll([
      GYM_PERMISSIONS.CREATE_USER,
      GYM_PERMISSIONS.ASSIGN_USER_ROLE
    ])
  );

  has(permission: string): boolean {
    const snapshot = this.snapshot();
    const user = this.auth.user();

    return user !== null &&
      snapshot?.user === user &&
      snapshot.permissions.has(permission);
  }

  hasAll(permissions: readonly string[]): boolean {
    return this.ready() &&
      permissions.every(permission => this.has(permission));
  }

  async load(): Promise<void> {
    const version = ++this.requestVersion;
    const user = this.auth.user();

    this.snapshot.set(null);
    this.error.set('');

    if (!user) {
      this.loading.set(false);
      throw new Error('Login required');
    }

    this.loading.set(true);

    try {
      const response = await firstValueFrom(
        this.http.get<ApiResponse<MyAccess>>('/get-my-access', {
          withCredentials: true,
          timeout: 15000
        })
      );

      const permissions = response?.payload?.permissions;

      if (
        response?.success !== true ||
        !Array.isArray(permissions) ||
        !permissions.every(
          permission =>
            typeof permission === 'string' &&
            permission.trim().length > 0
        )
      ) {
        throw new Error('Invalid access response');
      }

      // Discard responses belonging to an older request/session.
      if (
        version !== this.requestVersion ||
        this.auth.user() !== user
      ) {
        throw new Error('Access request superseded');
      }

      this.snapshot.set({
        user,
        permissions: new Set(permissions)
      });
    } catch (error: unknown) {
      if (
        version === this.requestVersion &&
        this.auth.user() === user
      ) {
        this.snapshot.set(null);
        this.error.set(
          'Unable to load your access. Please try again.'
        );
      }

      throw error;
    } finally {
      if (version === this.requestVersion) {
        this.loading.set(false);
      }
    }
  }

  clear(): void {
    ++this.requestVersion;
    this.snapshot.set(null);
    this.loading.set(false);
    this.error.set('');
  }
}