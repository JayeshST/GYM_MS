package com.app.gym.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class GymRegisterUserRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be within 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Size(max = 254, message = "Email must be within 254 characters")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Mobile number must contain 10 to 15 digits"
    )
    private String mobileNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must be within 500 characters")
    private String address;

    @NotBlank(message = "Password is required")
    @Size(min = 12, max = 72,
          message = "Password must contain 12 to 72 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Role code is required")
    @Size(max = 50, message = "Role code is too long")
    private String roleCode;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}