package com.app.gym.service;

public interface GymAdminBootstrapService {

    boolean createInitialAdmin(
            String fullName,
            String email,
            String mobileNumber,
            String password);
}