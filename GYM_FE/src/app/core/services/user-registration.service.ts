import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, Observable, switchMap } from 'rxjs';

import {
  RegisterUserRequest,
  RegisterUserResponse
} from '../models/user.models';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  payload: T | null;
  errors: string[];
}

interface CsrfPayload {
  headerName: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class UserRegistrationService {
  private readonly http = inject(HttpClient);

  register(
    request: RegisterUserRequest
  ): Observable<RegisterUserResponse> {
    return this.http
      .get<ApiResponse<CsrfPayload>>('/get-csrf-token', {
        withCredentials: true,
        timeout: 15000
      })
      .pipe(
        switchMap(response => {
          const csrf = response?.payload;

          if (
            response?.success !== true ||
            !csrf ||
            csrf.headerName !== 'X-XSRF-TOKEN' ||
            typeof csrf.token !== 'string' ||
            !csrf.token
          ) {
            throw new Error('INVALID_CSRF_RESPONSE');
          }

          return this.http.post<ApiResponse<RegisterUserResponse>>(
            '/register-user',
            request,
            {
              withCredentials: true,
              headers: new HttpHeaders({
                'X-XSRF-TOKEN': csrf.token
              }),
              timeout: 20000
            }
          );
        }),
        map(response => {
          if (
            response?.success !== true ||
            !response.payload ||
            !Number.isSafeInteger(response.payload.userId) ||
            response.payload.userId <= 0
          ) {
            throw new Error('INVALID_REGISTRATION_RESPONSE');
          }

          return response.payload;
        })
      );
  }
}