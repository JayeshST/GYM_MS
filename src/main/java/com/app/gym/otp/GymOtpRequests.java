package com.app.gym.otp;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public final class GymOtpRequests {

    private GymOtpRequests() {
    }

    /*
     * Reject unexpected request fields.
     */
    public static class Strict {

        @JsonAnySetter
        public void unknown(String fieldName, Object value) {

            throw new IllegalArgumentException(
                    "Unsupported request field");
        }
    }

    /*
     * Starts password recovery using email and mobile.
     */
    public static class Forgot extends Strict {

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        @Size(
            max = 254,
            message = "Email must be within 254 characters"
        )
        public String email;

        @NotBlank(message = "Mobile number is required")
        @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Mobile number must contain 10 to 15 digits"
        )
        public String mobileNumber;
    }

    /*
     * Identifies a challenge created by the backend.
     */
    public static class Challenge extends Strict {

        @NotBlank(message = "Challenge ID is required")
        @Pattern(
            regexp = "^[a-f0-9]{64}$",
            message = "Invalid challenge ID"
        )
        public String challengeId;
    }

    /*
     * Used for login OTP and recovery OTP verification.
     */
    public static class Verify extends Challenge {

        @NotBlank(message = "OTP is required")
        @Pattern(
            regexp = "^[0-9]{4,8}$",
            message = "OTP must contain 4 to 8 digits"
        )
        public String otp;
    }

    /*
     * Sets a new password after recovery OTP verification.
     */
    public static class Reset extends Challenge {

        @NotBlank(message = "Reset token is required")
        @Pattern(
            regexp = "^[a-f0-9]{64}$",
            message = "Invalid reset token"
        )
        public String resetToken;

        @NotBlank(message = "New password is required")
        @Size(
            min = 12,
            max = 72,
            message = "Password must contain 12 to 72 characters"
        )
        public String newPassword;

        @NotBlank(message = "Confirm password is required")
        @Size(
            max = 72,
            message = "Confirm password must be within 72 characters"
        )
        public String confirmPassword;
    }
}