package com.app.gym.dao;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.security.repository.GymAuthorizationRepository;

@Repository
public class GymAuthorizationDao {

    private final GymAuthorizationRepository authorizationRepository;

    public GymAuthorizationDao(
            GymAuthorizationRepository authorizationRepository) {

        this.authorizationRepository = authorizationRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasActivePermission(
            Long userId,
            String permissionCode) {

        if (userId == null
                || permissionCode == null
                || permissionCode.trim().isEmpty()) {
            return false;
        }

        long permissionCount =
                authorizationRepository.countActivePermissionMappings(
                        userId,
                        permissionCode);

        return permissionCount > 0;
    }
    @Transactional(readOnly = true)
    public List<String> getActivePermissionCodes(Long userId) {

        if (userId == null) {
            return Collections.emptyList();
        }

        return authorizationRepository.findActivePermissionCodes(userId);
    }
}