package com.app.gym.user.repository;

import java.util.Optional;
import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.app.gym.common.entity.GymUserEntity;
public interface GymUserRepository
        extends JpaRepository<GymUserEntity, Long> {

    Optional<GymUserEntity> findByUsernameIgnoreCaseAndIsDeleted(
            String username,
            Integer isDeleted);

    boolean existsByUsernameIgnoreCase(String username);

    @Query(
        "SELECT u FROM GymUserEntity u " +
        "WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email)) " +
        "AND u.isDeleted = 0"
    )
    Optional<GymUserEntity> findLoginUserByEmail(
            @Param("email") String email);

    @Query(
        "SELECT COUNT(u.id) FROM GymUserEntity u " +
        "WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(:email))"
    )
    
    
    long countUsersByEmail(
            @Param("email") String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM GymUserEntity u WHERE u.id = :userId")
    Optional<GymUserEntity> findUserForPasswordChange(
            @Param("userId") Long userId);
    
    
}