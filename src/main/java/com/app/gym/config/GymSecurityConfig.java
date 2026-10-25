package com.app.gym.config;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.app.gym.config.GymSessionValidationFilter;
import com.app.gym.user.repository.GymUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class GymSecurityConfig {

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {

        HttpSessionCsrfTokenRepository repository =
                new HttpSessionCsrfTokenRepository();

        repository.setHeaderName("X-XSRF-TOKEN");

        return repository;
    }

    @Bean
    public HttpSessionSecurityContextRepository securityContextRepository() {

        HttpSessionSecurityContextRepository repository =
                new HttpSessionSecurityContextRepository();

        repository.setDisableUrlRewriting(true);

        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            HttpSessionCsrfTokenRepository csrfTokenRepository,
            HttpSessionSecurityContextRepository securityContextRepository,
            ObjectMapper objectMapper,
            GymUserRepository userRepository) throws Exception {

        http
            .securityContext()
                .securityContextRepository(securityContextRepository)
            .and()
            .csrf()
                .csrfTokenRepository(csrfTokenRepository)
            .and()
            .formLogin()
                .disable()
            .httpBasic()
                .disable()
            .logout()
                .disable()
            .requestCache()
                .disable()
            .authorizeRequests()
                .antMatchers(
                        HttpMethod.GET,
                        "/get-csrf-token")
                    .permitAll()
                .antMatchers(
                        HttpMethod.POST,
                        "/user-login")
                    .permitAll()
                .antMatchers(
                        HttpMethod.GET,
                        "/get-current-user")
                    .authenticated()
                .antMatchers(
                        HttpMethod.POST,
                        "/user-logout",
                        "/change-password")
                    .authenticated()
                    .antMatchers(
                            HttpMethod.POST,
                            "/register-user")
                        .authenticated()
                        .antMatchers(
                                HttpMethod.GET,
                                "/get-user-list")
                            .authenticated()
                            .antMatchers(
                                    HttpMethod.GET,
                                    "/get-user/*")
                                .authenticated()
                               
                              
                .anyRequest()
                    .denyAll()
            .and()
            .exceptionHandling()
                .authenticationEntryPoint(
                    (request, response, exception) ->
                        writeError(
                            response,
                            objectMapper,
                            401,
                            "Login required"
                        )
                )
                .accessDeniedHandler(
                    (request, response, exception) ->
                        writeError(
                            response,
                            objectMapper,
                            403,
                            "Access denied or invalid CSRF token"
                        )
                );

        http.addFilterBefore(
                new GymSessionValidationFilter(
                        userRepository,
                        objectMapper),
                FilterSecurityInterceptor.class);

        return http.build();
    }

    private void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String message) throws IOException {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("success", false);
        body.put("status", status);
        body.put("message", message);
        body.put("payload", null);
        body.put("errors", Collections.emptyList());

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        objectMapper.writeValue(
                response.getOutputStream(),
                body);
    }
}