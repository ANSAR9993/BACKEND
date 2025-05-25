package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.AuthDto.AuthRequest;
import com.dgapr.demo.Dto.AuthDto.AuthResponse;
import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Model.User.User;
import com.dgapr.demo.Repository.UserRepository;
import com.dgapr.demo.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Core authentication logic:
 *  - verifies username existence & account status
 *  - delegates to AuthenticationManager for credential check23

 *  - issues JWT on success
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    /**
     * Attempt to authenticate with the given credentials.
     * Handles account suspension/deletion and returns localized messages.
     *
     * @param request AuthRequest containing raw username & password
     * @return AuthResponse with a success flag, message, token, and role
     */
    public AuthResponse authenticate(AuthRequest request) {
        String requestUsername = request.username();
        if (requestUsername == null || requestUsername.isBlank()) {
            log.error("Username in request is null");
            return AuthResponse.builder()
                    .success(false)
                    .message("Nom d’utilisateur invalide")
                    .build();
        }
        String username = requestUsername.trim().toLowerCase();

        String requestPassword = request.password();
        if (requestPassword == null || requestPassword.isBlank()) {
            log.error("Password in request is null or blank for user: {}", username);
            return AuthResponse.builder()
                    .success(false)
                    .message("Mot de passe invalide")
                    .build();
        }
        String password = requestPassword.trim();
        log.debug("Authentication attempt for '{}'", username);

        // Lookup
        var userLookup = userRepository.findByUsername(username);
        if (userLookup.isEmpty()) {
            log.error("Authentication failed: user not found: {}", username);
            return AuthResponse.builder()
                    .success(false)
                    .message("Nom d’utilisateur ou mot de passe invalide")
                    .build();
        }

        // Credentials
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password
                    )
            );

            log.debug("Authentication successful: {}", username);
            User principal = (User) auth.getPrincipal();
            String role = (principal.getRole() != null)
                    ? principal.getRole().name()
                    : Role.USER.name();

            String token = tokenProvider.generateToken(auth);
            return AuthResponse.builder()
                    .success(true)
                    .message("Authentification réussie")
                    .token(token)
                    .role(role)
                    .build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", username);
            return AuthResponse.builder()
                    .success(false)
                    .message("Nom d’utilisateur ou mot de passe invalide")
                    .build();
        } catch (DisabledException e) {
            log.error("Authentication failed for user: {} – account disabled", username);
            return AuthResponse.builder()
                    .success(false)
                    .message("Votre compte est verrouillé. Contactez l’administrateur.")
                    .build();
        } catch (LockedException e) {
            log.warn("Account locked for user: {}", username);
            return AuthResponse.builder()
                    .success(false)
                    .message("Utilisateur suspendu, contactez l’administrateur")
                    .build();
        } catch (InternalAuthenticationServiceException e) {
            log.error("Internal authentication service error for user {}: {}", username, e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Une erreur s’est produite lors de l’authentification")
                    .build();
        } catch (AuthenticationException e) {
            log.error("Other authentication error for user {}: {}", username, e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Une erreur s’est produite lors de l’authentification")
                    .build();
        } catch (Exception e) {
            log.error("Unexpected critical error during authentication for user {}: {}", username, e.getMessage(), e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Une erreur s’est produite lors de l’authentification")
                    .build();
        }
    }
}


