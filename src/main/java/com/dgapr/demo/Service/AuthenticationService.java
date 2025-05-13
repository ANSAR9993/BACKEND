package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.AuthDto.AuthRequest;
import com.dgapr.demo.Dto.AuthDto.AuthResponse;
import com.dgapr.demo.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse authenticate(AuthRequest request) {
        log.debug("Attempting authentication for user: {}", request.username());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            log.debug("Authentication successful for user: {}", request.username());

            String token = tokenProvider.generateToken(authentication);
            return AuthResponse.builder()
                    .success(true)
                    .message("Authentication successful")
                    .token(token)
                    .tokenType("Bearer")
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for {}: {}", request.username(), e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid username or password")
                    .build();
        } catch (DisabledException e) {
            log.warn("User {} is disabled: {}", request.username(), e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message("User is disabled, contact administrator")
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication for {}: {}", request.username(), e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message("An unexpected error occurred")
                    .build();
        }
    }
}

