package com.app.gym.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.gym.service.GymUserDetailsService;


@Configuration
public class GymAuthenticationConfig {

    @Bean
    public DaoAuthenticationProvider gymAuthenticationProvider(
            GymUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // Unknown email and wrong password are treated
        // as invalid credentials.
        provider.setHideUserNotFoundExceptions(true);

        return provider;
    }

    @Bean
    public AuthenticationManager gymAuthenticationManager(
            DaoAuthenticationProvider gymAuthenticationProvider) {

        ProviderManager manager = new ProviderManager(
                Collections.singletonList(
                        gymAuthenticationProvider));

        manager.setEraseCredentialsAfterAuthentication(true);

        return manager;
    }
}