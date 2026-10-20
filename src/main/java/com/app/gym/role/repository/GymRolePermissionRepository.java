package com.app.gym.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gym.role.entity.GymRolePermissionEntity;

public interface GymRolePermissionRepository
        extends JpaRepository<GymRolePermissionEntity, Long> {

    Optional<GymRolePermissionEntity>
        findByRole_IdAndPermission_Id(
            Long roleId,
            Long permissionId);
}