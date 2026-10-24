import { HttpErrorResponse } from '@angular/common/http';

export function apiErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Server connection failed. Check the backend and try again.';
    }

    if (error.status >= 500) {
      return 'The server could not complete the request. Please try again.';
    }

    const body = error.error;

    if (body && typeof body === 'object') {
      if (Array.isArray(body.errors)) {
        const messages = body.errors.filter(
          (item: unknown): item is string => typeof item === 'string'
        );

        if (messages.length > 0) {
          return messages.join(' ');
        }
      }

      if (typeof body.message === 'string') {
        return body.message;
      }
    }

    return `Request failed (${error.status}). Please try again.`;
  }

  return 'The request could not be completed. Please try again.';
}