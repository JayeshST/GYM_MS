package com.app.gym.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.audit.GymAuditEventEntity;
import com.app.gym.audit.repository.GymAuditEventRepository;
import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.common.entity.GymUserRoleEntity;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.dao.GymBootstrapGuardDao;
import com.app.gym.role.entity.GymRoleEntity;
import com.app.gym.role.repository.GymRoleRepository;

import com.app.gym.user.repository.GymUserRepository;
import com.app.gym.user.repository.GymUserRoleRepository;

@Service
public class GymAdminBootstrapServiceImpl
        implements GymAdminBootstrapService {

    private final GymBootstrapGuardDao guardDao;
    private final GymUserRepository userRepository;
    private final GymRoleRepository roleRepository;
    private final GymUserRoleRepository userRoleRepository;
    private final GymAuditEventRepository auditRepository;
    private final GymAuthorizationDao authorizationDao;
    private final PasswordEncoder passwordEncoder;

    public GymAdminBootstrapServiceImpl(
            GymBootstrapGuardDao guardDao,
            GymUserRepository userRepository,
            GymRoleRepository roleRepository,
            GymUserRoleRepository userRoleRepository,
            GymAuditEventRepository auditRepository,
            GymAuthorizationDao authorizationDao,
            PasswordEncoder passwordEncoder) {

        this.guardDao = guardDao;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditRepository = auditRepository;
        this.authorizationDao = authorizationDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public boolean createInitialAdmin(
            String fullName,
            String email,
            String mobileNumber,
            String password) {

        if (guardDao.lockAndCheckCompleted()) {
            return false;
        }

        validateInputs(fullName, email, mobileNumber, password);

        if (userRepository.count() > 0) {
            throw new IllegalStateException(
                    "Bootstrap requires an empty user table. " +
                    "Existing accounts need a reviewed migration.");
        }

        GymRoleEntity adminRole = roleRepository
                .findByRoleCodeAndIsActiveAndIsDeleted(
                        "ADMIN", 1, 0)
                .orElseThrow(() -> new IllegalStateException(
                        "Active ADMIN role is missing"));

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        GymUserEntity admin = new GymUserEntity();

        // Internal username retained for the existing NOT NULL column.
        // Application login will use email.
        admin.setUsername("initial_admin");
        admin.setFullName(fullName.trim());
        admin.setEmail(normalizedEmail);
        admin.setMobileNumber(mobileNumber.trim());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setMustChangePassword(1);
        admin.setAuthVersion(0L);
        admin.setIsActive(1);
        admin.setIsDeleted(0);

        admin = userRepository.saveAndFlush(admin);

        Long adminId = admin.getId();

        // First account is the bootstrap actor.
//        admin.setCreatedBy(adminId);
        admin.setUpdatedBy(adminId);

        GymUserRoleEntity mapping = new GymUserRoleEntity();

        mapping.setUser(admin);
        mapping.setRole(adminRole);
        mapping.setIsActive(1);
        mapping.setIsDeleted(0);
        mapping.setCreatedBy(adminId);
        mapping.setUpdatedBy(adminId);

        userRoleRepository.saveAndFlush(mapping);

        requirePermission(
                adminId,
                "CAN-CREATE-GYM-USER");

        requirePermission(
                adminId,
                "CAN-ASSIGN-GYM-USER-ROLE");

        requirePermission(
                adminId,
                "CAN-MANAGE-GYM-ROLE-PERMISSION");

        GymAuditEventEntity auditEvent =
                GymAuditEventEntity.success(
                        adminId,
                        "ADMIN_BOOTSTRAPPED",
                        "USER",
                        adminId,
                        null,
                        "{\"roleCode\":\"ADMIN\"}",
                        "Initial administrator created by operator setup");

        auditRepository.save(auditEvent);

        guardDao.markCompleted();

        return true;
    }

    private void requirePermission(
            Long userId,
            String permissionCode) {

        if (!authorizationDao.hasActivePermission(
                userId, permissionCode)) {

            throw new IllegalStateException(
                    "ADMIN role is missing required permission: "
                    + permissionCode);
        }
    }

    private void validateInputs(
            String fullName,
            String email,
            String mobileNumber,
            String password) {

        if (fullName == null
                || fullName.trim().isEmpty()
                || fullName.trim().length() > 150) {

            throw new IllegalArgumentException(
                    "Admin name is required and must be within 150 characters");
        }

        if (email == null
                || email.trim().length() > 254
                || !email.trim().matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new IllegalArgumentException(
                    "A valid Admin email is required");
        }

        if (mobileNumber == null
                || !mobileNumber.trim().matches(
                        "^\\+?[0-9]{10,15}$")) {

            throw new IllegalArgumentException(
                    "Admin mobile number must contain 10 to 15 digits");
        }

        if (password == null
                || password.trim().isEmpty()
                || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters " +
                    "and at most 72 UTF-8 bytes");
        }
    }
}