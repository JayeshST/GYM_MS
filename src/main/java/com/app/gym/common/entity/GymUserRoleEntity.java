package com.app.gym.common.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.app.gym.role.entity.GymRoleEntity;

@Entity
@Table(
    name = "GYM_MAP_USER_TO_ROLE",
    uniqueConstraints = @UniqueConstraint(
        name = "UK_GYM_USER_ROLE",
        columnNames = {"USER_ID", "ROLE_ID"}
    )
)
public class GymUserRoleEntity extends GymBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private GymUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private GymRoleEntity role;

    public GymUserRoleEntity() {
    }

    public GymUserEntity getUser() {
        return user;
    }

    public void setUser(GymUserEntity user) {
        this.user = user;
    }

    public GymRoleEntity getRole() {
        return role;
    }

    public void setRole(GymRoleEntity role) {
        this.role = role;
    }
}