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

export interface CurrentUserRole {
  roleId: number;
  roleCode: string;
  roleName: string;
}

export interface CurrentUser {
  userId: number;
  fullName: string;
  email: string;
  mustChangePassword: boolean;
  roles: CurrentUserRole[];
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