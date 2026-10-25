package com.app.gym.service;

import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Service;

import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dao.GymCurrentUserRoleDao;
import com.app.gym.dto.GymLoginRequestDto;
import com.app.gym.dto.GymLoginResponseDto;
import com.app.gym.user.repository.GymUserRepository;

@Service
public class GymLoginServiceImpl implements GymLoginService {

    private final AuthenticationManager authenticationManager;
    private final GymUserRepository userRepository;
    private final GymLoginAuditService auditService;
    private final HttpSessionCsrfTokenRepository csrfTokenRepository;
    private final HttpSessionSecurityContextRepository securityContextRepository;
    private final GymCurrentUserRoleDao currentUserRoleDao;

    private final ChangeSessionIdAuthenticationStrategy sessionStrategy =
            new ChangeSessionIdAuthenticationStrategy();

    public GymLoginServiceImpl(
            @Qualifier("gymAuthenticationManager")
            AuthenticationManager authenticationManager,
            GymUserRepository userRepository,
            GymLoginAuditService auditService,
            HttpSessionCsrfTokenRepository csrfTokenRepository,
            HttpSessionSecurityContextRepository securityContextRepository,
            GymCurrentUserRoleDao currentUserRoleDao) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.csrfTokenRepository = csrfTokenRepository;
        this.securityContextRepository = securityContextRepository;
        this.currentUserRoleDao = currentUserRoleDao;
    }

    @Override
    public GymLoginResponseDto loginUser(
            GymLoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        Authentication authentication;
        GymUserEntity user;

        try {
            String email = loginRequest.getEmail()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            UsernamePasswordAuthenticationToken credentials =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            loginRequest.getPassword());

            authentication =
                    authenticationManager.authenticate(credentials);

            GymAuthenticatedUser principal =
                    (GymAuthenticatedUser)
                            authentication.getPrincipal();

            user = findValidUser(principal);

        } catch (AuthenticationException exception) {

            clearSession(request);
            auditService.recordLoginFailure();

            throw new BadCredentialsException(
                    "Invalid email or password");
        }

        try {
            // Prepare the complete response before recording success.
            GymLoginResponseDto payload = toResponse(user);

            request.getSession(true);

            // Rotate the session ID after authentication.
            sessionStrategy.onAuthentication(
                    authentication,
                    request,
                    response);

            // Discard the pre-login CSRF token.
            csrfTokenRepository.saveToken(
                    null,
                    request,
                    response);

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(
                    context,
                    request,
                    response);

            auditService.recordLoginSuccess(user.getId());

            return payload;

        } catch (RuntimeException exception) {

            // Do not leave an authenticated session after setup failure.
            clearSession(request);
            throw exception;
        }
    }

    @Override
    public GymLoginResponseDto getCurrentUser(
            HttpServletRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof GymAuthenticatedUser)) {

            clearSession(request);

            throw new BadCredentialsException(
                    "Login required");
        }

        GymAuthenticatedUser principal =
                (GymAuthenticatedUser)
                        authentication.getPrincipal();

        try {
            GymUserEntity user = findValidUser(principal);

            // Read current active roles from the database.
            return toResponse(user);

        } catch (AuthenticationException exception) {

            clearSession(request);

            throw new BadCredentialsException(
                    "Session is no longer valid. Please login again");
        }
    }

    private GymUserEntity findValidUser(
            GymAuthenticatedUser principal) {

        GymUserEntity user = userRepository
                .findById(principal.getUserId())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid account"));

        boolean valid =
                Integer.valueOf(0).equals(user.getIsInactive())
                && Integer.valueOf(0).equals(user.getIsDeleted())
                && Objects.equals(
                        user.getAuthVersion(),
                        principal.getAuthVersion());

        if (!valid) {
            throw new BadCredentialsException(
                    "Invalid account or session");
        }

        return user;
    }

    private GymLoginResponseDto toResponse(
            GymUserEntity user) {

        return new GymLoginResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                Integer.valueOf(1).equals(
                        user.getMustChangePassword()),
                currentUserRoleDao.findActiveRoles(user.getId())
        );
    }

    private void clearSession(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public void logoutUser(
            HttpServletRequest request,
            HttpServletResponse response) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        Long userId = null;

        if (authentication != null
                && authentication.getPrincipal()
                        instanceof GymAuthenticatedUser) {

            GymAuthenticatedUser principal =
                    (GymAuthenticatedUser)
                            authentication.getPrincipal();

            userId = principal.getUserId();
        }

        SecurityContextLogoutHandler logoutHandler =
                new SecurityContextLogoutHandler();

        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        logoutHandler.setSecurityContextRepository(
                securityContextRepository);

        logoutHandler.logout(
                request,
                response,
                authentication);

        if (userId != null) {
            auditService.recordLogoutSuccess(userId);
        }
    }
}