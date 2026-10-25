export interface MyAccess {
  permissions: string[];
}

export const GYM_PERMISSIONS = {
  VIEW_USERS: 'CAN-VIEW-GYM-USER-LIST',
  CREATE_USER: 'CAN-CREATE-GYM-USER',
  ASSIGN_USER_ROLE: 'CAN-ASSIGN-GYM-USER-ROLE'
} as const;