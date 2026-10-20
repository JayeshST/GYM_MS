package com.app.gym.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.gym.dto.GymCsrfResponseDto;

@RestController
public class GymCsrfController {

    @GetMapping("/get-csrf-token")
    public ResponseEntity<Map<String, Object>> getCsrfToken(
            CsrfToken csrfToken) {

        GymCsrfResponseDto payload =
                new GymCsrfResponseDto(
                        csrfToken.getHeaderName(),
                        csrfToken.getToken());

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("success", true);
        response.put("status", 200);
        response.put("message", "CSRF token generated");
        response.put("payload", payload);
        response.put("errors", Collections.emptyList());

        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(response);
    }
}