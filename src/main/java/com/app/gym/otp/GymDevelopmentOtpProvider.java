package com.app.gym.otp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GymDevelopmentOtpProvider implements GymOtpProvider {

    public GymDevelopmentOtpProvider(
            Environment environment,
            @Value("${gym.otp.mode:unconfigured}") String mode) {

        Set<String> activeProfiles = new HashSet<>(
                Arrays.asList(environment.getActiveProfiles())
        );

        Set<String> allowedProfiles = new HashSet<>(
                Arrays.asList("dev", "local", "test")
        );

        boolean validConfiguration =
                "dev-static".equals(mode)
                        && activeProfiles.contains("dev")
                        && allowedProfiles.containsAll(activeProfiles);

        if (!validConfiguration) {

            throw new IllegalStateException(
                    "Static OTP requires the dev profile "
                            + "and gym.otp.mode=dev-static. "
                            + "Install a real OTP provider "
                            + "before production."
            );
        }
    }

    @Override
    public String generate() {
        return "1234";
    }

    @Override
    public void send(
            String registeredEmail,
            String otp,
            String purpose) {


    }
}