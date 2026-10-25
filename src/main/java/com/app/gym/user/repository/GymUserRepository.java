package com.app.gym.user.repository;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.dto.GymUserDetailsResponseDto;
import com.app.gym.dto.GymUserListItemDto;

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
    @Query(
        "SELECT u FROM GymUserEntity u " +
        "WHERE u.id = :userId"
    )
    Optional<GymUserEntity> findUserForPasswordChange(
            @Param("userId") Long userId);

    @Query(
        value =
            "SELECT new com.app.gym.dto.GymUserListItemDto(" +
            "u.id, u.fullName, u.email, u.mobileNumber, " +
            "u.isInactive, u.isDeleted, u.createdAt) " +
            "FROM GymUserEntity u " +
            "WHERE (:isDeleted IS NULL OR u.isDeleted = :isDeleted) " +
            "AND (:isInactive IS NULL OR u.isInactive = :isInactive) " +
            "AND (:search IS NULL " +
            "OR LOCATE(:search, LOWER(u.fullName)) > 0 " +
            "OR LOCATE(:search, LOWER(u.email)) > 0 " +
            "OR LOCATE(:search, u.mobileNumber) > 0)",

        countQuery =
            "SELECT COUNT(u.id) FROM GymUserEntity u " +
            "WHERE (:isDeleted IS NULL OR u.isDeleted = :isDeleted) " +
            "AND (:isInactive IS NULL OR u.isInactive = :isInactive) " +
            "AND (:search IS NULL " +
            "OR LOCATE(:search, LOWER(u.fullName)) > 0 " +
            "OR LOCATE(:search, LOWER(u.email)) > 0 " +
            "OR LOCATE(:search, u.mobileNumber) > 0)"
    )
    Page<GymUserListItemDto> searchUserList(
            @Param("search") String search,
            @Param("isInactive") Integer isInactive,
            @Param("isDeleted") Integer isDeleted,
            Pageable pageable);

    @Query(
        "SELECT new com.app.gym.dto.GymUserDetailsResponseDto(" +
        "u.id, u.fullName, u.email, u.mobileNumber, u.address, " +
        "u.isInactive, u.isDeleted, u.createdAt, u.updatedAt) " +
        "FROM GymUserEntity u " +
        "WHERE u.id = :userId"
    )
    Optional<GymUserDetailsResponseDto> findUserDetails(
            @Param("userId") Long userId);
}