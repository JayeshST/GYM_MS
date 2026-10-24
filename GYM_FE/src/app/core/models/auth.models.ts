export interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  payload: T | null;
  errors: string[];
}

export interface CsrfToken {
  headerName: string;
  token: string;
}

export interface CurrentUser {
  userId: number;
  fullName: string;
  email: string;
  mustChangePassword: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}