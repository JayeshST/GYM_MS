package com.app.gym.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.gym.common.entity.GymUserRoleEntity;

public interface GymUserRoleRepository
        extends JpaRepository<GymUserRoleEntity, Long> {

    Optional<GymUserRoleEntity> findByUser_IdAndRole_Id(
            Long userId,
            Long roleId);

    @Query(
        "SELECT ur FROM GymUserRoleEntity ur " +
        "JOIN FETCH ur.role r " +
        "WHERE ur.user.id IN :userIds " +
        "AND ur.isDeleted = 0 " +
        "AND r.isDeleted = 0 " +
        "ORDER BY ur.user.id, r.roleCode"
    )
    List<GymUserRoleEntity> findDisplayRolesByUserIds(
            @Param("userIds") Collection<Long> userIds);
}