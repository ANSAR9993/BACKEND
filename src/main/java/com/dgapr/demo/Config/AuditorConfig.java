package com.dgapr.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

/**
 * Spring configuration for auditing.
 *
 * <p>
 * Provides an {@link AuditorAware} bean that returns the username of the currently authenticated user
 * for auditing purposes. If no user is authenticated, it defaults to "SYSTEM".
 * </p>
 */
@Configuration
public class AuditorConfig {

    /**
     * Returns an {@link AuditorAware} implementation that retrieves the current auditor's username
     * from the Spring Security context.
     *
     * @return an {@link AuditorAware} of type String
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Principal::getName)
                .or(() -> Optional.of("SYSTEM"));
    }
}

