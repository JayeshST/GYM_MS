package com.app.gym.role.entity;

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
@Table(name = "GYM_MST_ROLE")
public class GymRoleEntity extends GymBaseEntity {

    @Column(name = "ROLE_CODE", nullable = false, length = 50)
    private String roleCode;

    @Column(name = "ROLE_NAME", nullable = false, length = 100)
    private String roleName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
}