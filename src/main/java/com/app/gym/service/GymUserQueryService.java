package com.app.gym.service;

import com.app.gym.dto.GymUserDetailsResponseDto;
import com.app.gym.dto.GymUserListResponseDto;

public interface GymUserQueryService {

    GymUserListResponseDto getUserList(
            String search,
            Integer isActive,
            int page,
            int size);

    GymUserDetailsResponseDto getUserDetails(Long userId);
}