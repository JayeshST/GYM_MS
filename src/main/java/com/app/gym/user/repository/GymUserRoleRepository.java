package com.app.gym.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gym.common.entity.GymUserRoleEntity;



public interface GymUserRoleRepository
        extends JpaRepository<GymUserRoleEntity, Long> {

    Optional<GymUserRoleEntity> findByUser_IdAndRole_Id(
            Long userId,
            Long roleId);
}