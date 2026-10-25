package com.app.gym.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gym.permission.entity.GymPermissionEntity;

public interface GymRolePermissionRepository
        extends JpaRepository<GymPermissionEntity, Long> {

    Optional<GymPermissionEntity>
        findByPermissionCodeAndIsInactiveAndIsDeleted(
            String permissionCode,
            Integer isInactive,
            Integer isDeleted);
}