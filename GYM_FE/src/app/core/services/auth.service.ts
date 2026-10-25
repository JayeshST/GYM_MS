import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import {
  ApiResponse,
  ChangePasswordRequest,
  CsrfToken,
  CurrentUser,
  LoginRequest
} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly userState = signal<CurrentUser | null>(null);

  readonly user = this.userState.asReadonly();

  private requirePayload<T>(response: ApiResponse<T>): T {
    if (
      !response ||
      response.success !== true ||
      response.payload == null
    ) {
      throw new Error('Invalid API response');
    }

    return response.payload;
  }

  private readCurrentUser(
    response: ApiResponse<CurrentUser>
  ): CurrentUser {
    const user = this.requirePayload(response);

    if (
      !Number.isSafeInteger(user.userId) ||
      user.userId <= 0 ||
      typeof user.fullName !== 'string' ||
      typeof user.email !== 'string' ||
      typeof user.mustChangePassword !== 'boolean' ||
      !Array.isArray(user.roles)
    ) {
      throw new Error('Invalid current user response');
    }

    const validRoles = user.roles.every(role =>
      role != null &&
      Number.isSafeInteger(role.roleId) &&
      role.roleId > 0 &&
      typeof role.roleCode === 'string' &&
      role.roleCode.trim().length > 0 &&
      typeof role.roleName === 'string'
    );

    if (!validRoles) {
      throw new Error('Invalid user roles response');
    }

    return {
      userId: user.userId,
      fullName: user.fullName,
      email: user.email,
      mustChangePassword: user.mustChangePassword,
      roles: user.roles.map(role => ({
        roleId: role.roleId,
        roleCode: role.roleCode,
        roleName: role.roleName
      }))
    };
  }

  async restoreSession(): Promise<CurrentUser> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CurrentUser>>('/get-current-user', {
        withCredentials: true
      })
    );

    const user = this.readCurrentUser(response);
    this.userState.set(user);

    return user;
  }

  private async csrfHeaders(): Promise<HttpHeaders> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CsrfToken>>('/get-csrf-token', {
        withCredentials: true
      })
    );

    const csrf = this.requirePayload(response);

    if (
      csrf.headerName !== 'X-XSRF-TOKEN' ||
      typeof csrf.token !== 'string' ||
      !csrf.token
    ) {
      throw new Error('Invalid CSRF response');
    }

    return new HttpHeaders({
      'X-XSRF-TOKEN': csrf.token
    });
  }

  async login(request: LoginRequest): Promise<CurrentUser> {
    const headers = await this.csrfHeaders();

    const response = await firstValueFrom(
      this.http.post<ApiResponse<CurrentUser>>(
        '/user-login',
        {
          email: request.email.trim(),
          password: request.password
        },
        {
          headers,
          withCredentials: true
        }
      )
    );

    const user = this.readCurrentUser(response);
    this.userState.set(user);

    return user;
  }

  async changePassword(request: ChangePasswordRequest): Promise<void> {
    const headers = await this.csrfHeaders();

    const response = await firstValueFrom(
      this.http.post<ApiResponse<null>>(
        '/change-password',
        request,
        {
          headers,
          withCredentials: true
        }
      )
    );

    if (!response || response.success !== true) {
      throw new Error('Password change failed');
    }

    this.clearSession();
  }

  async logout(): Promise<void> {
    const headers = await this.csrfHeaders();

    const response = await firstValueFrom(
      this.http.post<ApiResponse<null>>(
        '/user-logout',
        {},
        {
          headers,
          withCredentials: true
        }
      )
    );

    if (!response || response.success !== true) {
      throw new Error('Logout failed');
    }

    this.clearSession();
  }

  clearSession(): void {
    this.userState.set(null);
  }
}