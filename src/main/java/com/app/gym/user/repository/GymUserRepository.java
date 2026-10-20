package com.app.gym.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gym.common.entity.GymUserEntity;

public interface GymUserRepository
        extends JpaRepository<GymUserEntity, Long> {

    Optional<GymUserEntity> findByUsernameIgnoreCaseAndIsDeleted(
            String username,
            Integer isDeleted);

    boolean existsByUsernameIgnoreCase(String username);
}