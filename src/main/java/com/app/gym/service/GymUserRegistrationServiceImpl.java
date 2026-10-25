package com.app.gym.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.admin.GymAdminDao;
import com.app.gym.audit.GymAuditEventEntity;
import com.app.gym.audit.repository.GymAuditEventRepository;
import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.common.entity.GymUserRoleEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.dto.GymRegisterUserRequestDto;
import com.app.gym.dto.GymRegisterUserResponseDto;
import com.app.gym.role.entity.GymRoleEntity;
import com.app.gym.role.repository.GymRoleRepository;
import com.app.gym.user.repository.GymUserRepository;
import com.app.gym.user.repository.GymUserRoleRepository;

@Service
public class GymUserRegistrationServiceImpl
        implements GymUserRegistrationService {

    private static final String CREATE_USER_PERMISSION =
            "CAN-CREATE-GYM-USER";

    private static final String ASSIGN_USER_ROLE_PERMISSION =
            "CAN-ASSIGN-GYM-USER-ROLE";

    private final GymAdminDao adminDao;
    private final GymUserRepository userRepository;
    private final GymRoleRepository roleRepository;
    private final GymUserRoleRepository userRoleRepository;
    private final GymAuthorizationDao authorizationDao;
    private final GymAuditEventRepository auditRepository;
    private final PasswordEncoder passwordEncoder;

    public GymUserRegistrationServiceImpl(
            GymAdminDao adminDao,
            GymUserRepository userRepository,
            GymRoleRepository roleRepository,
            GymUserRoleRepository userRoleRepository,
            GymAuthorizationDao authorizationDao,
            GymAuditEventRepository auditRepository,
            PasswordEncoder passwordEncoder) {

        this.adminDao = adminDao;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.authorizationDao = authorizationDao;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public GymRegisterUserResponseDto registerUser(
            GymRegisterUserRequestDto request) {

        adminDao.lockAdministration();

        GymUserEntity actor = getValidActor();

        requirePermission(
                actor.getId(),
                CREATE_USER_PERMISSION
        );

        requirePermission(
                actor.getId(),
                ASSIGN_USER_ROLE_PERMISSION
        );

        validateRequest(request);
        validatePassword(request);

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String roleCode = request.getRoleCode()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!"ADMIN".equals(roleCode)
                && !"TRAINER".equals(roleCode)
                && !"RECEPTIONIST".equals(roleCode)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role must be ADMIN, TRAINER or RECEPTIONIST"
            );
        }

        if (userRepository.countUsersByEmail(normalizedEmail) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        GymRoleEntity role = roleRepository
                .findByRoleCodeAndIsInactiveAndIsDeleted(
                        roleCode,
                        0,
                        0
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Requested role is not available"
                        ));

        GymUserEntity user = new GymUserEntity();

        // Internal username; authentication uses email.
        user.setUsername("gym_" + UUID.randomUUID());

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setMobileNumber(request.getMobileNumber().trim());
        user.setAddress(request.getAddress().trim());

        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        user.setIsInactive(0);
        user.setIsDeleted(0);

        // First login requires the user to change this password.
        user.setMustChangePassword(1);
        user.setAuthVersion(0L);

        user.setCreatedBy(actor.getId());
        user.setUpdatedBy(actor.getId());

        user = userRepository.saveAndFlush(user);

        GymUserRoleEntity mapping = new GymUserRoleEntity();

        mapping.setUser(user);
        mapping.setRole(role);

        mapping.setIsInactive(0);
        mapping.setIsDeleted(0);

        mapping.setCreatedBy(actor.getId());
        mapping.setUpdatedBy(actor.getId());

        userRoleRepository.saveAndFlush(mapping);

        // roleCode has been restricted to three fixed values.
        // Passwords and password hashes are excluded from audit data.
        String afterData =
                "{\"roleCode\":\"" + roleCode + "\","
                        + "\"isInactive\":0,"
                        + "\"isDeleted\":0,"
                        + "\"mustChangePassword\":1}";

        auditRepository.save(
                GymAuditEventEntity.success(
                        actor.getId(),
                        "USER_REGISTERED",
                        "USER",
                        user.getId(),
                        null,
                        afterData,
                        "Staff account created and role assigned"
                )
        );

        return new GymRegisterUserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                role.getRoleCode(),
                Integer.valueOf(1).equals(
                        user.getMustChangePassword())
        );
    }

    private GymUserEntity getValidActor() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof GymAuthenticatedUser)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        GymAuthenticatedUser principal =
                (GymAuthenticatedUser)
                        authentication.getPrincipal();

        GymUserEntity actor = userRepository
                .findById(principal.getUserId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Session is no longer valid"
                        ));

        boolean valid =
                Integer.valueOf(0).equals(actor.getIsInactive())
                        && Integer.valueOf(0).equals(
                                actor.getIsDeleted())
                        && actor.getAuthVersion() != null
                        && Objects.equals(
                                actor.getAuthVersion(),
                                principal.getAuthVersion()
                        );

        if (!valid) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Session is no longer valid"
            );
        }

        if (!Integer.valueOf(0).equals(
                actor.getMustChangePassword())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Change your password before registering staff"
            );
        }

        return actor;
    }

    private void requirePermission(
            Long userId,
            String permissionCode) {

        if (!authorizationDao.hasActivePermission(
                userId,
                permissionCode)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access not available"
            );
        }
    }

    private void validateRequest(
            GymRegisterUserRequestDto request) {

        if (request == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Registration details are required"
            );
        }

        requireText(
                request.getFullName(),
                "Full name",
                150
        );

        requireText(
                request.getEmail(),
                "Email",
                254
        );

        requireText(
                request.getMobileNumber(),
                "Mobile number",
                20
        );

        requireText(
                request.getAddress(),
                "Address",
                500
        );

        requireText(
                request.getRoleCode(),
                "Role code",
                50
        );

        if (!request.getEmail().trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid email is required"
            );
        }

        if (!request.getMobileNumber().trim().matches(
                "^\\+?[0-9]{10,15}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mobile number must contain 10 to 15 digits"
            );
        }
    }

    private void requireText(
            String value,
            String fieldName,
            int maxLength) {

        if (value == null || value.trim().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        if (value.trim().length() > maxLength) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be within "
                            + maxLength + " characters"
            );
        }
    }

    private void validatePassword(
            GymRegisterUserRequestDto request) {

        String password = request.getPassword();

        if (password == null
                || password.trim().isEmpty()
                || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 12 characters "
                            + "and at most 72 UTF-8 bytes"
            );
        }

        if (!password.equals(request.getConfirmPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password and confirm password do not match"
            );
        }
    }
}