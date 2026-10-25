export type BinaryFlag = 0 | 1;
export type FlagFilter = '' | '0' | '1';

export interface UserRoleSummary {
  roleId: number;
  roleCode: string;
  roleName: string;
  isInactive: BinaryFlag;
  mappingIsInactive: BinaryFlag;
}

export interface UserListItem {
  userId: number;
  fullName: string;
  email: string;
  mobileNumber: string | null;
  isInactive: BinaryFlag;
  isDeleted: BinaryFlag;
  createdAt: string | null;
  roles: UserRoleSummary[];
}

export interface UserPage {
  users: UserListItem[];
  page: number;
  size: number;
  numberOfElements: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface UserDetails extends UserListItem {
  address: string | null;
  updatedAt: string | null;
}

export interface UserSearch {
  search: string;
  isInactive: FlagFilter;
  isDeleted: FlagFilter;
  page: number;
  size: number;
}

export type StaffRole = 'TRAINER' | 'RECEPTIONIST';

export interface RegisterUserRequest {
  fullName: string;
  email: string;
  mobileNumber: string;
  address: string;
  password: string;
  confirmPassword: string;
  roleCode: StaffRole;
}

export interface RegisterUserResponse {
  userId: number;
  fullName: string;
  email: string;
  roleCode: StaffRole;
  mustChangePassword: boolean;
}