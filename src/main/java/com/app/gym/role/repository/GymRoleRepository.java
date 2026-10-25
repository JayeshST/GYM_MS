package com.app.gym.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gym.role.entity.GymRoleEntity;

public interface GymRoleRepository
        extends JpaRepository<GymRoleEntity, Long> {

    Optional<GymRoleEntity> findByRoleCodeAndIsInactiveAndIsDeleted(
            String roleCode,
            Integer isInactive,
            Integer isDeleted);
}