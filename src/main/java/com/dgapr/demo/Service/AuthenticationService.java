package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.AuthDto.AuthRequest;
import com.dgapr.demo.Dto.AuthDto.AuthResponse;
import com.dgapr.demo.Model.Role;
import com.dgapr.demo.Model.User;
import com.dgapr.demo.Repository.UserRepository;
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
    private final UserRepository userRepository;

    public AuthResponse authenticate(AuthRequest request) {
        log.debug("Attempting authentication for user: {}", request.username().trim().toLowerCase());
        // Fetch user by username before authentication
        var userOpt = userRepository.findByUsername(request.username().trim().toLowerCase().trim().toLowerCase());
        if (userOpt.isEmpty()) {
            log.error("Authentication failed: user not found: {}", request.username().trim().toLowerCase());
            return AuthResponse.builder()
                    .success(false)
                    .message("Nom d’utilisateur ou mot de passe invalide")
                    .build();
        }
        var user = userOpt.get();
        switch (user.getStatus()) {
            case SUSPENDED -> {
                log.warn("Authentication failed for user: {} – account suspended", request.username().trim().toLowerCase());
                return AuthResponse.builder()
                        .success(false)
                        .message("Votre compte est suspendu. Veuillez contacter l'administrateur.")
                        .build();
            }
            case DELETED -> {
                log.warn("Authentication failed for user: {} – account deleted", request.username().trim().toLowerCase());
                return AuthResponse.builder()
                        .success(false)
                        .message("Votre compte a été supprimé. Veuillez contacter l'administrateur.")
                        .build();
            }
            default -> {}
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username().trim().toLowerCase(),
                            request.password().trim()
                    )
            );
            log.debug("Authentication successful for user: {}", request.username().trim().toLowerCase());
            User authenticatedUser = (User) authentication.getPrincipal();
            Role roleEnum = authenticatedUser.getRole();
            String role = (roleEnum != null) ? roleEnum.name() : "USER";
            String token = tokenProvider.generateToken(authentication);
            return AuthResponse.builder()
                    .success(true)
                    .message("Authentification réussie")
                    .token(token)
                    .role(role)
                    .build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", request.username().trim().toLowerCase());
            return AuthResponse.builder()
                    .success(false)
                    .message("Nom d’utilisateur ou mot de passe invalide")
                    .build();
        } catch (DisabledException e) {
            log.error("Authentication failed for user: {} – account disabled", request.username().trim().toLowerCase());
            return AuthResponse.builder()
                    .success(false)
                    .message("Utilisateur suspendu, contactez l’administrateur")
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message("Une erreur inattendue s’est produite")
                    .build();
        }
    }

}

