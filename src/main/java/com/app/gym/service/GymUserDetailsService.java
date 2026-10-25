
package com.app.gym.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.gym.common.entity.GymUserEntity;
import com.app.gym.config.GymAuthenticatedUser;
import com.app.gym.user.repository.GymUserRepository;

@Service
public class GymUserDetailsService implements UserDetailsService {

    private final GymUserRepository userRepository;

    public GymUserDetailsService(GymUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException(
                    "Invalid email or password");
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        GymUserEntity user = userRepository
                .findLoginUserByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Invalid email or password"));

        boolean enabled =
        		Integer.valueOf(0).equals(user.getIsInactive())
                && Integer.valueOf(0).equals(user.getIsDeleted());

        return new GymAuthenticatedUser(
                user.getId(),
                user.getEmail().trim().toLowerCase(Locale.ROOT),
                user.getPasswordHash(),
                enabled,
                user.getAuthVersion()
        );
    }
}