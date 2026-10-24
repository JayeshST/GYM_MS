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
    if (!response.success || response.payload === null) {
      throw new Error('Invalid API response');
    }

    return response.payload;
  }

  async restoreSession(): Promise<CurrentUser> {
    const response = await firstValueFrom(
      this.http.get<ApiResponse<CurrentUser>>('/get-current-user', {
        withCredentials: true
      })
    );

    const user = this.requirePayload(response);
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

    if (csrf.headerName !== 'X-XSRF-TOKEN' || !csrf.token) {
      throw new Error('Invalid CSRF response');
    }

    return new HttpHeaders({
      [csrf.headerName]: csrf.token
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

    const user = this.requirePayload(response);
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

    if (!response.success) {
      throw new Error('Password change failed');
    }

    // Backend invalidates the session after a successful password change.
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

    if (!response.success) {
      throw new Error('Logout failed');
    }

    this.clearSession();
  }

  clearSession(): void {
    this.userState.set(null);
  }
}