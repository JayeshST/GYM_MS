package com.app.gym.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.dto.GymRegisterUserRequestDto;
import com.app.gym.dto.GymRegisterUserResponseDto;
import com.app.gym.service.GymUserRegistrationService;

@RestController
public class GymUserRegistrationController {

    private final GymUserRegistrationService registrationService;

    public GymUserRegistrationController(
            GymUserRegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    @PostMapping("/register-user")
    public ResponseEntity<Map<String, Object>> registerUser(
            @Valid @RequestBody GymRegisterUserRequestDto request) {

        GymRegisterUserResponseDto payload =
                registrationService.registerUser(request);

        return buildResponse(
                201,
                true,
                "User registered successfully",
                payload,
                Collections.emptyList());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessError(
            ResponseStatusException exception) {

        return buildResponse(
                exception.getStatus().value(),
                false,
                exception.getReason(),
                null,
                Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(
            MethodArgumentNotValidException exception) {

        return buildResponse(
                400,
                false,
                "Validation failed",
                null,
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> error.getField()
                                + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBody(
            HttpMessageNotReadableException exception) {

        return buildResponse(
                400,
                false,
                "Invalid request body",
                null,
                Collections.emptyList());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataConflict(
            DataIntegrityViolationException exception) {

        // Includes a concurrent duplicate insert rejected by the DB.
        // Do not expose SQL, constraint internals or request passwords.
        return buildResponse(
                409,
                false,
                "Registration conflicts with existing data or database constraints",
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
}