package com.app.gym.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.app.gym.dto.GymLoginRequestDto;
import com.app.gym.dto.GymLoginResponseDto;


public interface GymLoginService {

    GymLoginResponseDto loginUser(
            GymLoginRequestDto loginRequest,
            HttpServletRequest request,
            HttpServletResponse response);

    GymLoginResponseDto getCurrentUser(
            HttpServletRequest request);

    void logoutUser(
            HttpServletRequest request,
            HttpServletResponse response);
}