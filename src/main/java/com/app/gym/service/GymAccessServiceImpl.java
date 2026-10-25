package com.app.gym.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymAuthorizationDao;
import com.app.gym.dto.GymMyAccessResponseDto;

@Service
public class GymAccessServiceImpl implements GymAccessService {

    private final GymAuthorizationDao authorizationDao;

    public GymAccessServiceImpl(
            GymAuthorizationDao authorizationDao) {

        this.authorizationDao = authorizationDao;
    }

    @Override
    @Transactional(readOnly = true)
    public GymMyAccessResponseDto getMyAccess() {

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

        Long userId = principal.getUserId();

        return new GymMyAccessResponseDto(
                userId,
                authorizationDao.getActivePermissionCodes(userId));
    }
}