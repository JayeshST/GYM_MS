package com.app.gym.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(
    name = "gym.bootstrap.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class GymAdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(GymAdminBootstrapRunner.class);

    private final GymAdminBootstrapService bootstrapService;

    @Value("${GYM_ADMIN_NAME:}")
    private String fullName;

    @Value("${GYM_ADMIN_EMAIL:}")
    private String email;

    @Value("${GYM_ADMIN_MOBILE:}")
    private String mobileNumber;

    @Value("${GYM_ADMIN_PASSWORD:}")
    private String password;

    public GymAdminBootstrapRunner(
            GymAdminBootstrapService bootstrapService) {

        this.bootstrapService = bootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {

        boolean created = bootstrapService.createInitialAdmin(
                fullName,
                email,
                mobileNumber,
                password);

        if (created) {
            log.info("Initial Admin created. Disable bootstrap configuration.");
        } else {
            log.info("Admin bootstrap already completed; no account created.");
        }
    }
}