package com.app.gym.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class GymLoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Size(max = 254, message = "Email must be within 254 characters")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public GymLoginRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}