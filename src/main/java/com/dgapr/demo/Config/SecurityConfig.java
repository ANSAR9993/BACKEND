 package com.dgapr.demo.Config; 

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Central Spring Security configuration.
 *
 * <p>Sets up stateless JWT authentication, role-based access rules,
 * CORS, and exception handling.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Bcrypt password encoder for hashing user passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager (needed for manual authentication).
     */
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * Returns an entry point that sends 401 with a JSON error payload.
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (req, res, ex) -> {
            res.setContentType("application/json");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + ex.getMessage() + "\"}");
        };
    }

    /**
     * Defines the security filter chain:
     *  – CORS enabled for localhost:5173
     *  – CSRF disabled
     *  – Stateless session (no cookies)
     *  – Public paths under /api/auth/**
     *  – Role-based locking of /api/users/** and /api/admin/**
     *  – JWT filter inserted before the username/password filter
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorizedHandler()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/login").permitAll()
                       .requestMatchers("/api/marches/societes").authenticated() // ⬅️ Require auth
                       .requestMatchers(HttpMethod.POST, "/api/materiels").authenticated()
                        .requestMatchers("/api/users/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/admin/**").hasRole(Role.SUPER_ADMIN.name())
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    

        return http.build();
   }
 


    /**
     * Configures CORS to allow requests from the React front-end.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //
        configuration.setAllowedOrigins(List.of("http://localhost:5173/"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}                  
