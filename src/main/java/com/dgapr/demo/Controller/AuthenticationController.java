package com.dgapr.demo.Controller;

import com.dgapr.demo.Audit.AuditContext;
import com.dgapr.demo.Dto.AuthDto.AuthRequest;
import com.dgapr.demo.Dto.AuthDto.AuthResponse;
import com.dgapr.demo.Model.User.User;
import com.dgapr.demo.Service.AuthenticationService;
import com.dgapr.demo.Service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder; // If needed, or use @AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Preferred way

import java.util.Map;

/**
 * REST endpoints for user authentication operations:
 * - /login: authenticate credentials and return JWT + role
 * - /logout: revoke current user's tokens
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    /**
     * Authenticate a user and return an AuthResponse containing
     * the JWT (on success) or an error message (on failure).
     *
     * @param request AuthRequest payload with username & password
     * @return 200 + AuthResponse on success; 401 + AuthResponse on bad credentials
     */
    
    @PostMapping("/login")
    
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authenticationService.authenticate(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    /**
     * Logs out the currently authenticated user by revoking their tokens.
     * The JWT must be provided in the Authorization header.
     *
     * @param authenticatedUser The currently authenticated user principal.
     * @return 200 + success message on successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal User authenticatedUser) {

        if (authenticatedUser == null) {
            log.warn("Logout attempt by unauthenticated user.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No authenticated user found to logout."));
        }
        try {
            log.info("Logging out user: {}", authenticatedUser.getUsername());
            AuditContext.disableAudit();
            userService.revokeTokens(authenticatedUser.getId());
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
        } catch (Exception e) {
            log.error("Error during logout for user {}: {}", authenticatedUser.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred during logout."));
        } finally {
            AuditContext.clear();
        }

    }
}

