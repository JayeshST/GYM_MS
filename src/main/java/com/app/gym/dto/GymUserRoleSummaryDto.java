package com.app.gym.dto;

public class GymUserRoleSummaryDto {

    private final Long roleId;
    private final String roleCode;
    private final String roleName;
    private final Integer isInactive;
    private final Integer mappingIsInactive;

    public GymUserRoleSummaryDto(
            Long roleId,
            String roleCode,
            String roleName,
            Integer isInactive,
            Integer mappingIsInactive) {

        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.isInactive = isInactive;
        this.mappingIsInactive = mappingIsInactive;
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

    public Integer getIsInactive() {
        return isInactive;
    }

    public Integer getMappingIsInactive() {
        return mappingIsInactive;
    }
}