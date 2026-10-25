import { inject, Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams
} from '@angular/common/http';
import { map, Observable } from 'rxjs';

import {
  UserDetails,
  UserListItem,
  UserPage,
  UserSearch
} from '../models/user.models';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  payload: T | null;
  errors: string[];
}

function unwrap<T>(response: ApiResponse<T>): T {
  if (
    !response ||
    response.success !== true ||
    response.payload == null
  ) {
    throw new Error('INVALID_API_RESPONSE');
  }

  return response.payload;
}

// Detect an old backend response instead of showing a wrong status.
function validateUser(user: UserListItem): void {
  if (
    !user ||
    !Number.isSafeInteger(user.userId) ||
    user.userId <= 0 ||
    (user.isInactive !== 0 && user.isInactive !== 1) ||
    (user.isDeleted !== 0 && user.isDeleted !== 1) ||
    !Array.isArray(user.roles)
  ) {
    throw new Error('INVALID_USER_RESPONSE');
  }

  for (const role of user.roles) {
    if (
      !role ||
      !Number.isSafeInteger(role.roleId) ||
      typeof role.roleCode !== 'string' ||
      typeof role.roleName !== 'string' ||
      (role.isInactive !== 0 && role.isInactive !== 1) ||
      (
        role.mappingIsInactive !== 0 &&
        role.mappingIsInactive !== 1
      )
    ) {
      throw new Error('INVALID_ROLE_RESPONSE');
    }
  }
}

export function userErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    switch (error.status) {
      case 0:
        return 'Unable to connect. Please check your connection and try again.';

      case 200:
        return 'We could not load the user information. Please contact your gym administrator.';

      case 400:
        return 'Please check your search and filter values.';

      case 401:
        return 'Your session has expired. Please sign in again.';

      case 403:
        return 'You cannot access this information right now. Please contact your gym administrator.';

      case 404:
        return 'The requested information could not be found.';

      case 429:
        return 'Too many requests. Please wait a moment and try again.';

      default:
        return 'Unable to load the information. Please try again later.';
    }
  }

  return 'We could not read the user information. Please contact your gym administrator.';
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getUsers(query: UserSearch): Observable<UserPage> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size);

    const search = query.search.trim();

    if (search) {
      params = params.set('search', search);
    }

    if (query.isInactive !== '') {
      params = params.set('isInactive', query.isInactive);
    }

    if (query.isDeleted !== '') {
      params = params.set('isDeleted', query.isDeleted);
    }

    return this.http.get<ApiResponse<UserPage>>(
      '/get-user-list',
      {
        params,
        withCredentials: true,
        timeout: 15000
      }
    ).pipe(
      map(response => {
        const page = unwrap(response);

        if (!Array.isArray(page.users)) {
          throw new Error('INVALID_USER_PAGE');
        }

        page.users.forEach(validateUser);
        return page;
      })
    );
  }

  getUser(userId: number): Observable<UserDetails> {
    return this.http.get<ApiResponse<UserDetails>>(
      `/get-user/${userId}`,
      {
        withCredentials: true,
        timeout: 15000
      }
    ).pipe(
      map(response => {
        const user = unwrap(response);
        validateUser(user);
        return user;
      })
    );
  }
}