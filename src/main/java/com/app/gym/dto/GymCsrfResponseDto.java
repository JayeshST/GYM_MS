package com.app.gym.dto;

public class GymCsrfResponseDto {

    private final String headerName;
    private final String token;

    public GymCsrfResponseDto(
            String headerName,
            String token) {

        this.headerName = headerName;
        this.token = token;
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getToken() {
        return token;
    }
}