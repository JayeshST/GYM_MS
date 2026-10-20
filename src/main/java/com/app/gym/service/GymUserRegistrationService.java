package com.app.gym.service;

import com.app.gym.dto.GymRegisterUserRequestDto;
import com.app.gym.dto.GymRegisterUserResponseDto;

public interface GymUserRegistrationService {

    GymRegisterUserResponseDto registerUser(
            GymRegisterUserRequestDto request);
}