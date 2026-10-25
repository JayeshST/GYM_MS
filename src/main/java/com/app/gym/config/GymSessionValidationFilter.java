package com.app.gym.config;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.user.repository.GymUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GymSessionValidationFilter extends OncePerRequestFilter {

    private final GymUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public GymSessionValidationFilter(
            GymUserRepository userRepository,
            ObjectMapper objectMapper) {

        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof GymAuthenticatedUser)) {

            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();

        // Allow an authenticated session to be closed even when
        // its account has since been disabled or changed.
        if ("POST".equals(request.getMethod())
                && "/user-logout".equals(path)) {

            filterChain.doFilter(request, response);
            return;
        }

        GymAuthenticatedUser principal =
                (GymAuthenticatedUser) authentication.getPrincipal();

        GymUserEntity user = userRepository
                .findById(principal.getUserId())
                .orElse(null);

        boolean valid =
                user != null
                && Integer.valueOf(0).equals(user.getIsInactive())
                && Integer.valueOf(0).equals(user.getIsDeleted())
                && user.getAuthVersion() != null
                && Objects.equals(
                        user.getAuthVersion(),
                        principal.getAuthVersion());

        if (!valid) {

            SecurityContextLogoutHandler logoutHandler =
                    new SecurityContextLogoutHandler();

            logoutHandler.logout(request, response, authentication);

            writeError(
                    response,
                    401,
                    "Session is no longer valid. Please login again");
            return;
        }

        if (Integer.valueOf(1).equals(user.getMustChangePassword())
                && !allowedBeforePasswordChange(request)) {

            writeError(
                    response,
                    403,
                    "Password change required before using this operation");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean allowedBeforePasswordChange(
            HttpServletRequest request) {

        String path = request.getServletPath();
        String method = request.getMethod();

        if ("GET".equals(method)) {
            return "/get-csrf-token".equals(path)
                    || "/get-current-user".equals(path);
        }

        if ("POST".equals(method)) {
            return "/change-password".equals(path)
                    || "/user-logout".equals(path)
                    || "/user-login".equals(path);
        }

        return false;
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", false);
        body.put("status", status);
        body.put("message", message);
        body.put("payload", null);
        body.put("errors", Collections.emptyList());

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}