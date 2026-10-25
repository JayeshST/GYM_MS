package com.app.gym.dto;

public class GymCurrentUserRoleDto {

    private final Long roleId;
    private final String roleCode;
    private final String roleName;

    public GymCurrentUserRoleDto(
            Long roleId,
            String roleCode,
            String roleName) {

        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }
}