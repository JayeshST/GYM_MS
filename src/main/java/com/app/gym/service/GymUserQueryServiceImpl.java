package com.app.gym.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.common.entity.GymUserRoleEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.dto.GymUserDetailsResponseDto;
import com.app.gym.dto.GymUserListItemDto;
import com.app.gym.dto.GymUserListResponseDto;
import com.app.gym.dto.GymUserRoleSummaryDto;
import com.app.gym.role.entity.GymRoleEntity;
import com.app.gym.user.repository.GymUserRepository;
import com.app.gym.user.repository.GymUserRoleRepository;

@Service
public class GymUserQueryServiceImpl implements GymUserQueryService {

    private static final String VIEW_USER_LIST_PERMISSION =
            "CAN-VIEW-GYM-USER-LIST";

    private final GymUserRepository userRepository;
    private final GymUserRoleRepository userRoleRepository;
    private final GymAuthorizationDao authorizationDao;

    public GymUserQueryServiceImpl(
            GymUserRepository userRepository,
            GymUserRoleRepository userRoleRepository,
            GymAuthorizationDao authorizationDao) {

        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.authorizationDao = authorizationDao;
    }

    @Override
    @Transactional(readOnly = true)
    public GymUserListResponseDto getUserList(
            String search,
            Integer isInactive,
            Integer isDeleted,
            int page,
            int size) {

        requireViewPermission();

        validateParameters(
                search,
                isInactive,
                isDeleted,
                page,
                size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id"));

        Page<GymUserListItemDto> result =
                userRepository.searchUserList(
                        normalizeSearch(search),
                        isInactive,
                        isDeleted,
                        pageable);

        List<Long> userIds = result.getContent()
                .stream()
                .map(GymUserListItemDto::getUserId)
                .collect(Collectors.toList());

        Map<Long, List<GymUserRoleSummaryDto>> rolesByUser =
                loadRoles(userIds);

        for (GymUserListItemDto user : result.getContent()) {
            user.setRoles(
                    rolesByUser.getOrDefault(
                            user.getUserId(),
                            Collections.emptyList()));
        }

        return new GymUserListResponseDto(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious());
    }

    @Override
    @Transactional(readOnly = true)
    public GymUserDetailsResponseDto getUserDetails(Long userId) {

        requireViewPermission();

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID must be greater than zero");
        }

        GymUserDetailsResponseDto details =
                userRepository.findUserDetails(userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));

        Map<Long, List<GymUserRoleSummaryDto>> rolesByUser =
                loadRoles(Collections.singletonList(userId));

        details.setRoles(
                rolesByUser.getOrDefault(
                        userId,
                        Collections.emptyList()));

        return details;
    }

    private void requireViewPermission() {

        GymUserEntity actor = getValidActor();

        if (!authorizationDao.hasActivePermission(
                actor.getId(),
                VIEW_USER_LIST_PERMISSION)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access not available");
        }
    }

    private GymUserEntity getValidActor() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof GymAuthenticatedUser)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required");
        }

        GymAuthenticatedUser principal =
                (GymAuthenticatedUser) authentication.getPrincipal();

        GymUserEntity actor = userRepository
                .findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Session is no longer valid"));

        boolean valid =
                Integer.valueOf(0).equals(actor.getIsInactive())
                && Integer.valueOf(0).equals(actor.getIsDeleted())
                && actor.getAuthVersion() != null
                && Objects.equals(
                        actor.getAuthVersion(),
                        principal.getAuthVersion());

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Session is no longer valid");
        }

        if (!Integer.valueOf(0).equals(actor.getMustChangePassword())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Change your password before using this operation");
        }

        return actor;
    }

    private Map<Long, List<GymUserRoleSummaryDto>> loadRoles(
            List<Long> userIds) {

        Map<Long, List<GymUserRoleSummaryDto>> result =
                new HashMap<>();

        if (userIds.isEmpty()) {
            return result;
        }

        List<GymUserRoleEntity> mappings =
                userRoleRepository.findDisplayRolesByUserIds(userIds);

        for (GymUserRoleEntity mapping : mappings) {

            GymRoleEntity role = mapping.getRole();

            GymUserRoleSummaryDto roleDto =
                    new GymUserRoleSummaryDto(
                            role.getId(),
                            role.getRoleCode(),
                            role.getRoleName(),
                            role.getIsInactive(),
                            mapping.getIsInactive());

            result.computeIfAbsent(
                    mapping.getUser().getId(),
                    key -> new ArrayList<>())
                    .add(roleDto);
        }

        return result;
    }

    private void validateParameters(
            String search,
            Integer isInactive,
            Integer isDeleted,
            int page,
            int size) {

        validateFlag(isInactive, "isInactive");
        validateFlag(isDeleted, "isDeleted");

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be zero or greater");
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100");
        }

        if ((long) page * size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested page is too large");
        }

        if (search != null && search.length() > 254) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search must be within 254 characters");
        }
    }

    private void validateFlag(Integer value, String fieldName) {

        if (value != null && value != 0 && value != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be 0 or 1");
        }
    }

    private String normalizeSearch(String search) {

        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        return search.trim().toLowerCase(Locale.ROOT);
    }
}