package com.app.gym.otp;

public interface GymOtpProvider {

	String generate();

	void send(String registeredEmail, String otp, String purpose);
}