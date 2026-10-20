package com.app.gym.service;

import com.app.gym.dto.GymChangePasswordRequestDto;

public interface GymPasswordService {

    void changePassword(GymChangePasswordRequestDto request);
}