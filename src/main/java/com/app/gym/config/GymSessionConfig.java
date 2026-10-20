package com.app.gym.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class GymSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(
            @Value("${gym.session.cookie-secure:true}")
            boolean cookieSecure) {

        DefaultCookieSerializer serializer =
                new DefaultCookieSerializer();

        serializer.setCookieName("GYMSESSION");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(cookieSecure);
        serializer.setSameSite("Lax");

        return serializer;
    }
}