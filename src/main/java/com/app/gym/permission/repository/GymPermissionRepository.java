package com.app.gym.permission.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gym.permission.entity.GymPermissionEntity;

public interface GymPermissionRepository
        extends JpaRepository<GymPermissionEntity, Long> {

    Optional<GymPermissionEntity>
        findByPermissionCodeAndIsActiveAndIsDeleted(
            String permissionCode,
            Integer isActive,
            Integer isDeleted);
}