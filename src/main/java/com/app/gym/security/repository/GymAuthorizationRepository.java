package com.app.gym.security.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.app.gym.common.entity.GymUserRoleEntity;

public interface GymAuthorizationRepository
        extends Repository<GymUserRoleEntity, Long> {

    @Query(
        "select count(ur.id) " +
        "from GymUserRoleEntity ur, GymRolePermissionEntity rp " +
        "where ur.role.id = rp.role.id " +
        "and ur.user.id = :userId " +

        "and ur.user.isInactive = 0 " +
        "and ur.user.isDeleted = 0 " +

        "and ur.isInactive = 0 " +
        "and ur.isDeleted = 0 " +

        "and ur.role.isInactive = 0 " +
        "and ur.role.isDeleted = 0 " +

        "and rp.isInactive = 0 " +
        "and rp.isDeleted = 0 " +

        "and rp.permission.isInactive = 0 " +
        "and rp.permission.isDeleted = 0 " +

        "and rp.permission.permissionCode = :permissionCode"
    )
    long countActivePermissionMappings(
            @Param("userId") Long userId,
            @Param("permissionCode") String permissionCode);

    @Query(
        "select distinct rp.permission.permissionCode " +
        "from GymUserRoleEntity ur, GymRolePermissionEntity rp " +
        "where ur.role.id = rp.role.id " +
        "and ur.user.id = :userId " +

        "and ur.user.isInactive = 0 " +
        "and ur.user.isDeleted = 0 " +

        "and ur.isInactive = 0 " +
        "and ur.isDeleted = 0 " +

        "and ur.role.isInactive = 0 " +
        "and ur.role.isDeleted = 0 " +

        "and rp.isInactive = 0 " +
        "and rp.isDeleted = 0 " +

        "and rp.permission.isInactive = 0 " +
        "and rp.permission.isDeleted = 0 " +

        "order by rp.permission.permissionCode"
    )
    List<String> findActivePermissionCodes(
            @Param("userId") Long userId);
}