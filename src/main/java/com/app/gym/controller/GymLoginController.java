package com.app.gym.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.app.gym.dto.GymChangePasswordRequestDto;
import com.app.gym.service.GymPasswordService;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.dto.GymLoginRequestDto;
import com.app.gym.dto.GymLoginResponseDto;
import com.app.gym.service.GymLoginService;

@RestController
public class GymLoginController {

    private final GymLoginService loginService;
    private final GymPasswordService passwordService;
    public GymLoginController(GymLoginService loginService,GymPasswordService passwordService) {
        this.loginService = loginService;
        this.passwordService = passwordService;
    }

    @PostMapping("/user-login")
    public ResponseEntity<Map<String, Object>> loginUser(
            @Valid @RequestBody GymLoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        Authentication existing =
                SecurityContextHolder.getContext().getAuthentication();

        if (existing != null
                && existing.isAuthenticated()
                && existing.getPrincipal() instanceof GymAuthenticatedUser) {

            return buildResponse(
                    409,
                    false,
                    "Already logged in. Logout before another login",
                    null,
                    Collections.emptyList());
        }

        GymLoginResponseDto payload = loginService.loginUser(
                loginRequest,
                request,
                response);

        return buildResponse(
                200,
                true,
                "Login successful",
                payload,
                Collections.emptyList());
    }

    @GetMapping("/get-current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            HttpServletRequest request) {

        return buildResponse(
                200,
                true,
                "Current user fetched successfully",
                loginService.getCurrentUser(request),
                Collections.emptyList());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailure(
            AuthenticationException exception) {

        return buildResponse(
                401,
                false,
                exception.getMessage(),
                null,
                Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFailure(
            MethodArgumentNotValidException exception) {

        return buildResponse(
                400,
                false,
                "Validation failed",
                null,
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField() + ": "
                                + error.getDefaultMessage())
                        .collect(Collectors.toList()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return buildResponse(
                400,
                false,
                "Invalid request body",
                null,
                Collections.emptyList());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status,
            boolean success,
            String message,
            Object payload,
            Object errors) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", success);
        body.put("status", status);
        body.put("message", message);
        body.put("payload", payload);
        body.put("errors", errors);

        return ResponseEntity.status(status)
                .header("Cache-Control", "no-store")
                .body(body);
    }
    
    @PostMapping("/user-logout")
    public ResponseEntity<Map<String, Object>> logoutUser(
            HttpServletRequest request,
            HttpServletResponse response) {

        loginService.logoutUser(request, response);

        return buildResponse(
                200,
                true,
                "Logout successful",
                null,
                Collections.emptyList());
    }
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody GymChangePasswordRequestDto passwordRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        // This service transaction commits before it returns.
        passwordService.changePassword(passwordRequest);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        SecurityContextLogoutHandler logoutHandler =
                new SecurityContextLogoutHandler();

        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);

        logoutHandler.logout(request, response, authentication);

        return buildResponse(
                200,
                true,
                "Password changed successfully. Please login again",
                null,
                Collections.emptyList());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInput(
            IllegalArgumentException exception) {

        return buildResponse(
                400,
                false,
                exception.getMessage(),
                null,
                Collections.emptyList());
    }
}