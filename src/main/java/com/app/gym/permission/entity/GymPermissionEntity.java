package com.app.gym.permission.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.app.gym.common.entity.GymBaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GYM_MST_PERMISSION")
public class GymPermissionEntity extends GymBaseEntity {

    @Column(name = "PERMISSION_CODE", nullable = false, length = 120)
    private String permissionCode;

    @Column(name = "PERMISSION_NAME", nullable = false, length = 150)
    private String permissionName;

    @Column(name = "MODULE_CODE", nullable = false, length = 50)
    private String moduleCode;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
}