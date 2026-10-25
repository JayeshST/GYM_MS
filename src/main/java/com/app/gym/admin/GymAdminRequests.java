package com.app.gym.admin;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public final class GymAdminRequests {

    private GymAdminRequests() {
    }

    /*
     * Common fields for administrative updates.
     *
     * version: current user-record version received from the API.
     * reason: reason for the administrative change.
     */
    public static class Revision {

        @NotNull(message = "Version is required")
        @Min(value = 0, message = "Version must be zero or greater")
        public Long version;

        @NotBlank(message = "Reason is required")
        @Size(
            max = 500,
            message = "Reason must be within 500 characters"
        )
        public String reason;

        /*
         * Reject fields not declared in the request DTO.
         * For example: email, roleCode, passwordHash or authVersion.
         */
        @JsonAnySetter
        public void unknown(String fieldName, Object value) {

            throw new IllegalArgumentException(
                    "Unsupported request field");
        }
    }

    /*
     * Editable profile fields only.
     * Email, username and role are intentionally not included.
     */
    public static class Edit extends Revision {

        @NotBlank(message = "Full name is required")
        @Size(
            max = 150,
            message = "Full name must be within 150 characters"
        )
        public String fullName;

        @NotBlank(message = "Mobile number is required")
        @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Mobile number must contain 10 to 15 digits"
        )
        public String mobileNumber;

        @NotBlank(message = "Address is required")
        @Size(
            max = 500,
            message = "Address must be within 500 characters"
        )
        public String address;
    }

    /*
     * 0 = Active
     * 1 = Inactive
     */
    public static class Status extends Revision {

        @NotNull(message = "Account status is required")
        @Min(
            value = 0,
            message = "isInactive must be 0 or 1"
        )
        @Max(
            value = 1,
            message = "isInactive must be 0 or 1"
        )
        public Integer isInactive;
    }

    /*
     * One user-specific permission override per request.
     */
    public static class Permission extends Revision {

        @NotBlank(message = "Permission code is required")
        @Size(
            max = 120,
            message = "Permission code must be within 120 characters"
        )
        public String permissionCode;

        @NotNull(message = "Permission effect is required")
        public Effect effect;
    }

    public enum Effect {
        GRANT,
        DENY,
        INHERIT
    }
}