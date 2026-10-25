package com.app.gym.admin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/users")
public class GymAdminUserController {

    private final GymAdminUserService service;

    public GymAdminUserController(GymAdminUserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> details(
            @PathVariable("id") Long userId) {

        return response(
                200,
                "User details fetched",
                service.details(userId)
        );
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> edit(
            @PathVariable("id") Long userId,
            @Valid @RequestBody GymAdminRequests.Edit request) {

        return response(
                200,
                "User profile updated",
                service.edit(userId, request)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> status(
            @PathVariable("id") Long userId,
            @Valid @RequestBody GymAdminRequests.Status request) {

        return response(
                200,
                "User status updated",
                service.status(userId, request)
        );
    }

    @PostMapping("/{id}/soft-delete")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable("id") Long userId,
            @Valid @RequestBody GymAdminRequests.Revision request) {

        return response(
                200,
                "User soft deleted",
                service.delete(userId, request)
        );
    }

    @PostMapping("/{id}/password-reset")
    public ResponseEntity<Map<String, Object>> requireReset(
            @PathVariable("id") Long userId,
            @Valid @RequestBody GymAdminRequests.Revision request) {

        return response(
                200,
                "Password recovery required. "
                        + "User must complete Forgot password.",
                service.requirePasswordReset(userId, request)
        );
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Object>> permissions(
            @PathVariable("id") Long userId) {

        return response(
                200,
                "User permissions fetched",
                service.permissions(userId)
        );
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Object>> permission(
            @PathVariable("id") Long userId,
            @Valid @RequestBody GymAdminRequests.Permission request) {

        return response(
                200,
                "User permission updated",
                service.permission(userId, request)
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> businessError(
            ResponseStatusException exception) {

        return response(
                exception.getStatus().value(),
                exception.getReason(),
                null
        );
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> invalidRequest(
            Exception exception) {

        return response(
                400,
                "Invalid request. Check required fields, "
                        + "values and unsupported fields",
                null
        );
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, Object>> concurrentChange(
            Exception exception) {

        return response(
                409,
                "Concurrent change. Reload details and retry",
                null
        );
    }

    private ResponseEntity<Map<String, Object>> response(
            int status,
            String message,
            Object payload) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", status < 400);
        body.put("status", status);
        body.put("message", message);
        body.put("payload", payload);
        body.put("errors", Collections.emptyList());

        return ResponseEntity.status(status)
                .header("Cache-Control", "no-store")
                .body(body);
    }
}