package com.app.gym.service;

import java.util.Locale;
import java.util.Objects;
import com.app.gym.user.repository.GymUserRepository;
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
import com.app.gym.dto.GymUserDetailsResponseDto;
import com.app.gym.user.repository.GymUserRepository;
import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.dto.GymUserDetailsResponseDto;
import com.app.gym.dto.GymUserListItemDto;
import com.app.gym.dto.GymUserListResponseDto;
import com.app.gym.user.repository.GymUserRepository;

@Service
public class GymUserQueryServiceImpl implements GymUserQueryService {

    private static final String VIEW_USER_LIST_PERMISSION =
            "CAN-VIEW-GYM-USER-LIST";

    private final GymUserRepository userRepository;
    private final GymAuthorizationDao authorizationDao;

    public GymUserQueryServiceImpl(
            GymUserRepository userRepository,
            GymAuthorizationDao authorizationDao) {

        this.userRepository = userRepository;
        this.authorizationDao = authorizationDao;
    }

    @Override
    @Transactional(readOnly = true)
    public GymUserListResponseDto getUserList(
            String search,
            Integer isActive,
            int page,
            int size) {

        GymUserEntity actor = getValidActor();

        if (!authorizationDao.hasActivePermission(
                actor.getId(),
                VIEW_USER_LIST_PERMISSION)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access not available");
        }

        validateParameters(search, isActive, page, size);

        String normalizedSearch = normalizeSearch(search);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id"));

        Page<GymUserListItemDto> result =
                userRepository.searchUserList(
                        normalizedSearch,
                        isActive,
                        pageable);

        return new GymUserListResponseDto(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext(),
                result.hasPrevious());
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
                Integer.valueOf(1).equals(actor.getIsActive())
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

    private void validateParameters(
            String search,
            Integer isActive,
            int page,
            int size) {

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

        // Prevent offsets larger than the JPA integer offset range.
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

        if (isActive != null
                && isActive != 0
                && isActive != 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "isActive must be 0 or 1");
        }
    }

    private String normalizeSearch(String search) {

        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        return search.trim().toLowerCase(Locale.ROOT);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public GymUserDetailsResponseDto getUserDetails(Long userId) {

        GymUserEntity actor = getValidActor();

        if (!authorizationDao.hasActivePermission(
                actor.getId(),
                VIEW_USER_LIST_PERMISSION)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access not available");
        }

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID must be greater than zero");
        }

        return userRepository.findUserDetails(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }
}