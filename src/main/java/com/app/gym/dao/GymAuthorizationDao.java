package com.app.gym.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.admin.GymAccessPolicy;
import com.app.gym.admin.GymAdminDao;
import com.app.gym.permission.entity.GymPermissionEntity;
import com.app.gym.security.repository.GymAuthorizationRepository;

@Repository
public class GymAuthorizationDao {

	private final GymAuthorizationRepository authorizationRepository;
	private final GymAdminDao adminDao;

	public GymAuthorizationDao(GymAuthorizationRepository authorizationRepository, GymAdminDao adminDao) {

		this.authorizationRepository = authorizationRepository;
		this.adminDao = adminDao;
	}

	@Transactional(readOnly = true)
	public boolean hasActivePermission(Long userId, String permissionCode) {

		if (permissionCode == null || permissionCode.trim().isEmpty()) {
			return false;
		}

		return getActivePermissionCodes(userId).contains(permissionCode);
	}

	@Transactional(readOnly = true)
	public List<String> getActivePermissionCodes(Long userId) {

		// Inactive, deleted or locked users have no effective access.
		if (userId == null || !adminDao.activeUser(userId)) {
			return Collections.emptyList();
		}

		Set<String> activePermissionCodes = new HashSet<>();
		Set<String> protectedPermissionCodes = new HashSet<>();

		List<GymPermissionEntity> permissionCatalog = adminDao.catalog();

		for (GymPermissionEntity permission : permissionCatalog) {

			String permissionCode = permission.getPermissionCode();

			activePermissionCodes.add(permissionCode);

			if (GymAccessPolicy.protectedPermission(permissionCode, permission.getModuleCode())) {

				protectedPermissionCodes.add(permissionCode);
			}
		}

		List<String> rolePermissions = authorizationRepository.findActivePermissionCodes(userId);

		boolean admin = adminDao.hasRole(userId, "ADMIN");

		return GymAccessPolicy.effective(rolePermissions, adminDao.overrides(userId), activePermissionCodes,
				protectedPermissionCodes, admin);
	}
}