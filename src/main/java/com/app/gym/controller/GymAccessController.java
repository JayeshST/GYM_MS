package com.app.gym.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.gym.dto.GymMyAccessResponseDto;
import com.app.gym.service.GymAccessService;

@RestController
public class GymAccessController {

    private final GymAccessService accessService;

    public GymAccessController(GymAccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/get-my-access")
    public ResponseEntity<Map<String, Object>> getMyAccess() {

        GymMyAccessResponseDto payload =
                accessService.getMyAccess();

        return buildResponse(
                200,
                true,
                "Access fetched successfully",
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
}