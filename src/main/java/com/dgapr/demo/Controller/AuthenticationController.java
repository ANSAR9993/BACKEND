package com.dgapr.demo.Controller;

import com.dgapr.demo.Dto.LoginDto.LoginRequest;
import com.dgapr.demo.Security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.debug("Login attempt for user: {}", loginRequest.username());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            String token = tokenProvider.generateToken(authentication);
            log.debug("Login successful for user: {}", loginRequest.username());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tokenType", "Bearer"
            ));

        } catch (BadCredentialsException ex) {
            log.error("Invalid credentials for user {}: {}", loginRequest.username(), ex.getMessage());
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", loginRequest.username());
            return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Authentication error for user {}: {}", loginRequest.username(), ex.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error"));
        }
    }

    // TODO: add /refresh, /forgot-password, /reset-password

}
