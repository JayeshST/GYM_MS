package com.app.gym.admin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.audit.GymAuditEventEntity;
import com.app.gym.audit.repository.GymAuditEventRepository;
import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.permission.entity.GymPermissionEntity;
import com.app.gym.security.repository.GymAuthorizationRepository;
import com.app.gym.user.repository.GymUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GymAdminUserService {

    private final GymAdminDao dao;
    private final GymUserRepository users;
    private final GymAuthorizationDao access;
    private final GymAuthorizationRepository roles;
    private final GymAuditEventRepository audits;
    private final ObjectMapper json;

    public GymAdminUserService(
            GymAdminDao dao,
            GymUserRepository users,
            GymAuthorizationDao access,
            GymAuthorizationRepository roles,
            GymAuditEventRepository audits,
            ObjectMapper json) {

        this.dao = dao;
        this.users = users;
        this.access = access;
        this.roles = roles;
        this.audits = audits;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> details(Long userId) {

        actor("CAN-VIEW-GYM-USER-LIST");

        return snapshot(target(userId, false));
    }

    @Transactional
    public Map<String, Object> edit(
            Long userId,
            GymAdminRequests.Edit request) {

        dao.lockAdministration();

        GymUserEntity actor =
                actor("CAN-UPDATE-GYM-USER");

        GymUserEntity user =
                writable(userId, request.version);

        Map<String, Object> before = snapshot(user);

        user.setFullName(request.fullName.trim());
        user.setMobileNumber(request.mobileNumber.trim());
        user.setAddress(request.address.trim());

        return finish(
                actor,
                user,
                before,
                "USER_PROFILE_UPDATED",
                request.reason
        );
    }

    @Transactional
    public Map<String, Object> status(
            Long userId,
            GymAdminRequests.Status request) {

        dao.lockAdministration();

        GymUserEntity actor =
                actor("CAN-CHANGE-GYM-USER-STATUS");

        GymUserEntity user =
                writable(userId, request.version);

        if (actor.getId().equals(userId)) {

            fail(
                    HttpStatus.CONFLICT,
                    "You cannot change your own account status"
            );
        }

        if (Integer.valueOf(1).equals(request.isInactive)) {
            protectLastAdmin(user);
        }

        Map<String, Object> before = snapshot(user);

        user.setIsInactive(request.isInactive);

        return finish(
                actor,
                user,
                before,
                "USER_STATUS_CHANGED",
                request.reason
        );
    }

    @Transactional
    public Map<String, Object> delete(
            Long userId,
            GymAdminRequests.Revision request) {

        dao.lockAdministration();

        GymUserEntity actor =
                actor("CAN-DELETE-GYM-USER");

        GymUserEntity user =
                writable(userId, request.version);

        if (actor.getId().equals(userId)) {

            fail(
                    HttpStatus.CONFLICT,
                    "You cannot delete your own account"
            );
        }

        protectLastAdmin(user);

        Map<String, Object> before = snapshot(user);

        user.setIsDeleted(1);
        user.setIsInactive(1);

        user.setDeletedAt(
                LocalDateTime.now(ZoneOffset.UTC));

        user.setDeletedBy(actor.getId());

        return finish(
                actor,
                user,
                before,
                "USER_SOFT_DELETED",
                request.reason
        );
    }

    @Transactional
    public Map<String, Object> requirePasswordReset(
            Long userId,
            GymAdminRequests.Revision request) {

        dao.lockAdministration();

        GymUserEntity actor =
                actor("CAN-RESET-GYM-USER-PASSWORD");

        GymUserEntity user =
                writable(userId, request.version);

        if (actor.getId().equals(userId)) {

            fail(
                    HttpStatus.CONFLICT,
                    "Use your own change-password operation"
            );
        }

        if (!Integer.valueOf(0).equals(user.getIsInactive())) {

            fail(
                    HttpStatus.CONFLICT,
                    "Activate the account before requesting recovery"
            );
        }

        protectLastAdmin(user);

        Map<String, Object> before = snapshot(user);

        user.setLoginLocked(1);
        user.setFailedLoginCount(0);

        Map<String, Object> result = finish(
                actor,
                user,
                before,
                "ADMIN_PASSWORD_RESET_REQUIRED",
                request.reason
        );

        result.put("nextAction", "FORGOT_PASSWORD");

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> permissions(Long userId) {

        actor("CAN-MANAGE-GYM-ROLE-PERMISSION");

        return permissionSnapshot(target(userId, false));
    }

    @Transactional
    public Map<String, Object> permission(
            Long userId,
            GymAdminRequests.Permission request) {

        dao.lockAdministration();

        GymUserEntity actor =
                actor("CAN-MANAGE-GYM-ROLE-PERMISSION");

        GymUserEntity user =
                writable(userId, request.version);

        if (actor.getId().equals(userId)
                || dao.hasRole(userId, "ADMIN")) {

            fail(
                    HttpStatus.FORBIDDEN,
                    "Individual overrides are only supported "
                            + "for Trainer and Receptionist accounts"
            );
        }

        if (!dao.hasRole(userId, "TRAINER")
                && !dao.hasRole(userId, "RECEPTIONIST")) {

            fail(
                    HttpStatus.CONFLICT,
                    "An active Trainer or Receptionist "
                            + "role assignment is required"
            );
        }

        GymPermissionEntity permission = dao.catalog()
                .stream()
                .filter(item ->
                        item.getPermissionCode()
                                .equals(request.permissionCode))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Active permission not found"
                        ));

        boolean protectedPermission =
                GymAccessPolicy.protectedPermission(
                        permission.getPermissionCode(),
                        permission.getModuleCode()
                );

        if (protectedPermission
                && request.effect == GymAdminRequests.Effect.GRANT) {

            fail(
                    HttpStatus.FORBIDDEN,
                    "Staff management permissions are reserved for Admin"
            );
        }

        if (request.effect == GymAdminRequests.Effect.GRANT
                && !access.hasActivePermission(
                        actor.getId(),
                        permission.getPermissionCode())) {

            fail(
                    HttpStatus.FORBIDDEN,
                    "You cannot grant a permission you do not hold"
            );
        }

        Map<String, Object> before =
                permissionSnapshot(user);

        dao.setOverride(
                userId,
                permission.getId(),
                request.effect.name(),
                actor.getId()
        );

        invalidate(user, actor.getId());

        users.flush();

        Map<String, Object> after =
                permissionSnapshot(user);

        audit(
                actor.getId(),
                userId,
                "USER_PERMISSION_CHANGED",
                before,
                after,
                request.reason
        );

        return after;
    }

    private GymUserEntity actor(String requiredPermission) {

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

        GymUserEntity actor = users
                .findById(principal.getUserId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Session is no longer valid"
                        ));

        if (!Integer.valueOf(0).equals(actor.getIsInactive())
                || !Integer.valueOf(0).equals(actor.getIsDeleted())
                || actor.getAuthVersion() == null
                || !Objects.equals(
                        actor.getAuthVersion(),
                        principal.getAuthVersion())) {

            fail(
                    HttpStatus.UNAUTHORIZED,
                    "Session is no longer valid"
            );
        }

        if (!Integer.valueOf(0).equals(
                actor.getMustChangePassword())) {

            fail(
                    HttpStatus.FORBIDDEN,
                    "Change your password first"
            );
        }

        if (!dao.hasRole(actor.getId(), "ADMIN")
                || !access.hasActivePermission(
                        actor.getId(),
                        requiredPermission)) {

            fail(
                    HttpStatus.FORBIDDEN,
                    "Access not available"
            );
        }

        return actor;
    }

    private GymUserEntity target(
            Long userId,
            boolean lock) {

        if (userId == null || userId <= 0) {

            fail(
                    HttpStatus.BAD_REQUEST,
                    "User ID must be positive"
            );
        }

        if (lock) {

            return users.findUserForPasswordChange(userId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "User not found"
                            ));
        }

        return users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));
    }

    private GymUserEntity writable(
            Long userId,
            Long expectedVersion) {

        GymUserEntity user = target(userId, true);

        if (!Integer.valueOf(0).equals(user.getIsDeleted())) {

            fail(
                    HttpStatus.CONFLICT,
                    "User has been deleted"
            );
        }

        if (expectedVersion == null
                || !Objects.equals(
                        expectedVersion,
                        user.getVersion())) {

            fail(
                    HttpStatus.CONFLICT,
                    "User changed. Reload details and try again"
            );
        }

        return user;
    }

    private void protectLastAdmin(GymUserEntity user) {

        boolean activeAndUnlocked =
                Integer.valueOf(0).equals(user.getIsInactive())
                        && Integer.valueOf(0).equals(
                                user.getLoginLocked());

        if (activeAndUnlocked
                && dao.hasRole(user.getId(), "ADMIN")
                && dao.activeAdminCount() <= 1) {

            fail(
                    HttpStatus.CONFLICT,
                    "The last active Admin cannot be disabled or deleted"
            );
        }
    }

    private void invalidate(
            GymUserEntity user,
            Long actorUserId) {

        user.setAuthVersion(
                Math.addExact(
                        Objects.requireNonNull(
                                user.getAuthVersion()),
                        1L
                )
        );

        user.setUpdatedBy(actorUserId);
    }

    private Map<String, Object> finish(
            GymUserEntity actor,
            GymUserEntity user,
            Map<String, Object> before,
            String action,
            String reason) {

        invalidate(user, actor.getId());

        users.flush();

        Map<String, Object> after = snapshot(user);

        Map<String, Object> safeBefore =
                new LinkedHashMap<>(before);

        Map<String, Object> safeAfter =
                new LinkedHashMap<>(after);

        // Avoid storing raw contact/profile values in audit JSON.
        for (String field : Arrays.asList(
                "fullName",
                "mobileNumber",
                "address",
                "email")) {

            safeBefore.remove(field);
            safeAfter.remove(field);
        }

        safeAfter.put(
                "changedFields",
                changedFields(before, after)
        );

        audit(
                actor.getId(),
                user.getId(),
                action,
                safeBefore,
                safeAfter,
                reason
        );

        return after;
    }

    private List<String> changedFields(
            Map<String, Object> before,
            Map<String, Object> after) {

        List<String> fields = new ArrayList<>();

        before.forEach((key, value) -> {

            if (!Objects.equals(value, after.get(key))) {
                fields.add(key);
            }
        });

        return fields;
    }

    private Map<String, Object> snapshot(GymUserEntity user) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("userId", user.getId());
        result.put("version", user.getVersion());

        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("mobileNumber", user.getMobileNumber());
        result.put("address", user.getAddress());

        result.put("isInactive", user.getIsInactive());
        result.put("isDeleted", user.getIsDeleted());

        result.put("loginLocked", user.getLoginLocked());
        result.put("failedLoginCount", user.getFailedLoginCount());

        return result;
    }

    private Map<String, Object> permissionSnapshot(
            GymUserEntity user) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("userId", user.getId());
        result.put("version", user.getVersion());

        result.put(
                "rolePermissions",
                roles.findActivePermissionCodes(user.getId())
        );

        result.put(
                "overrides",
                dao.overrides(user.getId())
        );

        result.put(
                "effectivePermissions",
                access.getActivePermissionCodes(user.getId())
        );

        List<Map<String, Object>> catalog = new ArrayList<>();

        for (GymPermissionEntity permission : dao.catalog()) {

            Map<String, Object> item = new LinkedHashMap<>();

            item.put(
                    "permissionCode",
                    permission.getPermissionCode()
            );

            item.put(
                    "permissionName",
                    permission.getPermissionName()
            );

            item.put(
                    "moduleCode",
                    permission.getModuleCode()
            );

            item.put(
                    "adminOnly",
                    GymAccessPolicy.protectedPermission(
                            permission.getPermissionCode(),
                            permission.getModuleCode()
                    )
            );

            catalog.add(item);
        }

        result.put("catalog", catalog);

        return result;
    }

    private void audit(
            Long actorUserId,
            Long targetUserId,
            String action,
            Object before,
            Object after,
            String reason) {

        try {

            audits.save(
                    GymAuditEventEntity.success(
                            actorUserId,
                            action,
                            "USER",
                            targetUserId,
                            json.writeValueAsString(before),
                            json.writeValueAsString(after),
                            reason.trim()
                    )
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to serialize audit event",
                    exception
            );
        }
    }

    private static void fail(
            HttpStatus status,
            String message) {

        throw new ResponseStatusException(status, message);
    }
}