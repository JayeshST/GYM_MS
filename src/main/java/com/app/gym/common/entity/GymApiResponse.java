package com.app.gym.common.entity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

public class GymApiResponse<T> {

    private final boolean success;
    private final int status;
    private final String message;
    private final T payload;
    private final List<String> errors;
    private final OffsetDateTime timestamp;

    public GymApiResponse(
            boolean success,
            int status,
            String message,
            T payload,
            List<String> errors) {

        this.success = success;
        this.status = status;
        this.message = message;
        this.payload = payload;
        this.errors = errors;
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static <T> GymApiResponse<T> success(
            int status,
            String message,
            T payload) {

        return new GymApiResponse<>(
                true,
                status,
                message,
                payload,
                Collections.emptyList());
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getPayload() {
        return payload;
    }

    public List<String> getErrors() {
        return errors;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}