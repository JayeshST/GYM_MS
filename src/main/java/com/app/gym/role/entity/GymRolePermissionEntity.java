package com.app.gym.role.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.app.gym.common.entity.GymBaseEntity;
import com.app.gym.permission.entity.GymPermissionEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "GYM_MAP_ROLE_TO_PERMISSION",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_GYM_ROLE_PERMISSION",
                columnNames = {"ROLE_ID", "PERMISSION_ID"}))
public class GymRolePermissionEntity extends GymBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private GymRoleEntity role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERMISSION_ID", nullable = false)
    private GymPermissionEntity permission;
}