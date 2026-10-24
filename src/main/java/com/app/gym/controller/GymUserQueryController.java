package com.app.gym.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.dto.GymUserDetailsResponseDto;
import com.app.gym.dto.GymUserListResponseDto;
import com.app.gym.service.GymUserQueryService;

@RestController
public class GymUserQueryController {

    private final GymUserQueryService userQueryService;

    public GymUserQueryController(
            GymUserQueryService userQueryService) {

        this.userQueryService = userQueryService;
    }

    @GetMapping("/get-user-list")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(name = "search", required = false)
            String search,

            @RequestParam(name = "isActive", required = false)
            Integer isActive,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "20")
            int size) {

        GymUserListResponseDto payload =
                userQueryService.getUserList(
                        search,
                        isActive,
                        page,
                        size);

        return buildResponse(
                200,
                true,
                "User list fetched successfully",
                payload);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessError(
            ResponseStatusException exception) {

        return buildResponse(
                exception.getStatus().value(),
                false,
                exception.getReason(),
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidParameter(
            MethodArgumentTypeMismatchException exception) {

        return buildResponse(
                400,
                false,
                "Invalid value for parameter: " + exception.getName(),
                null);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status,
            boolean success,
            String message,
            Object payload) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", success);
        body.put("status", status);
        body.put("message", message);
        body.put("payload", payload);
        body.put("errors", Collections.emptyList());

        return ResponseEntity.status(status)
                .header("Cache-Control", "no-store")
                .body(body);
    }
    @GetMapping("/get-user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetails(
            @PathVariable("userId") Long userId) {

        GymUserDetailsResponseDto payload =
                userQueryService.getUserDetails(userId);

        return buildResponse(
                200,
                true,
                "User details fetched successfully",
                payload);
    }
}