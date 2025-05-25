package com.dgapr.demo.Controller;

import com.dgapr.demo.Dto.UserDto.UserDto;
import com.dgapr.demo.Dto.UserDto.UserResponseDto;
import com.dgapr.demo.Model.User.User;
import com.dgapr.demo.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing user accounts and user-related operations.
 * Provides endpoints for user CRUD operations, token revocation, and retrieving current user details.
 * All administrative endpoints require the 'ADMIN' role.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * Retrieves a paginated list of users, with optional filtering capabilities.
     * Requires the calling user to have the 'ADMIN' role.
     *
     * @param pageable   Spring Data {@link Pageable} object for pagination and sorting (default size 20, sorted by createdAt DESC).
     * @param filterParams A map of key-value pairs representing filtering criteria for users.
     * @return A {@link ResponseEntity} containing a {@link Page} of {@link UserResponseDto} objects.
     * @apiNote Requires 'ADMIN' role.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam Map<String,String> filterParams
    ) {
        Page<UserResponseDto> result = userService.getUsers(pageable, filterParams);
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves a single user by their unique ID.
     * Requires the calling user to have the 'ADMIN' role.
     *
     * @param id The UUID of the user to retrieve.
     * @return A {@link ResponseEntity} containing the {@link UserResponseDto} if found,
     * or an error message with {@code HttpStatus.NOT_FOUND} if the user does not exist,
     * or {@code HttpStatus.INTERNAL_SERVER_ERROR} for other issues.
     * @apiNote Requires 'ADMIN' role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (EntityNotFoundException e) {
            log.warn("Warn getting user: User not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", String.format("User with ID %s not found", id)));
        } catch (Exception e) {
            log.error("Error getting user with id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Creates a new user account.
     * Requires the calling user to have the 'ADMIN' role.
     *
     * @param userDto A {@link UserDto} object containing the details for the new user,
     * validated using {@code @Valid}.
     * @return A {@link ResponseEntity} with a success message and username if creation is successful,
     * or an error message with {@code HttpStatus.BAD_REQUEST} if validation fails
     * or user data is invalid (e.g., duplicate username/email/ID number).
     * @apiNote Requires 'ADMIN' role.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            log.debug("Creating user with data: {}", userDto);
            UserDto user = userService.createUser(userDto);
            log.debug("User created successfully: {}", user);
            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur créé avec succès",
                    "username", user.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating user: Invalid data provided - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Updates an existing user account identified by their ID.
     * Requires the calling user to have the 'ADMIN' role.
     *
     * @param id      The UUID of the user to update.
     * @param userDto A {@link UserDto} object containing the updated user details,
     * validated using {@code @Valid}.
     * @return A {@link ResponseEntity} with a success message and updated user data if successful,
     * or an error message with {@code HttpStatus.NOT_FOUND} if the user does not exist,
     * or {@code HttpStatus.BAD_REQUEST} if validation fails or data is invalid.
     * @apiNote Requires 'ADMIN' role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
        try {
            log.debug("Updating user {} with data: {}", id, userDto);
            UserDto user = userService.updateUser(id, userDto);
            log.debug("User updated successfully: {}", user);
            return ResponseEntity.ok(Map.of(
                    "message", String.format("User ' %s ' has been successfully updated", user.getUsername()),
                    "user", user
            ));
        } catch (EntityNotFoundException e) {
            log.warn("Warn update user: User not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", String.format("Cannot update: User with ID %s not found", id)));
        } catch (IllegalArgumentException e) {
            log.error("Error update user: Invalid user data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Deletes a user account by their ID. This is a soft-delete operation,
     * meaning the user's {@code Is_Deleted} flag is set to true and status to {@code DELETED}.
     * Requires the calling user to have the 'ADMIN' role.
     *
     * @param id The UUID of the user to delete.
     * @return A {@link ResponseEntity} with a success message and details of the deleted user,
     * or an error message with {@code HttpStatus.NOT_FOUND} if the user does not exist,
     * or {@code HttpStatus.INTERNAL_SERVER_ERROR} for other issues.
     * @apiNote Requires 'ADMIN' role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        try {
            log.debug("Deleting user with id: {}", id);
            // Get user details before deletion
            User user = modelMapper.map(userService.getUserById(id), User.class);

            userService.deleteUser(id);
            log.debug("User deleted successfully");

            String message = String.format(
                    "User ' %s ' has been successfully deleted",
                    user.getUsername().toUpperCase()
            );

            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "deletedUser", Map.of(
                            "id", user.getId(),
                            "id_number", user.getIdNumber(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "role", user.getRole().name()
                    )
            ));
        } catch (EntityNotFoundException e) {
            log.warn("warn deleting user: User not found with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", String.format("Cannot delete: User with ID %s not found", id)));
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Retrieves the details of the currently authenticated user.
     * This endpoint is accessible to any authenticated user.
     *
     * @param userDetails The {@link UserDetails} object representing the currently authenticated user,
     * injected by Spring Security.
     * @return A {@link ResponseEntity} containing a map of the current user's details
     * (id, username, email, role, first name, last name),
     * or an error message with {@code HttpStatus.NOT_FOUND} if the user is not found,
     * or {@code HttpStatus.INTERNAL_SERVER_ERROR} for other issues.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserResponseDto user = userService.loadUserByUsername(userDetails.getUsername());
            log.info("user is finde", user.getUsername());
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "firstname", user.getFirstname(),
                    "lastname", user.getLastname()
            ));
        } catch (UsernameNotFoundException e) {
            log.error("Error getting current user: User not found: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal Server Error"));
        }
    }

    /**
     * Revokes all active tokens for a specific user, forcing them to re-authenticate.
     * This is an administrative operation.
     * Requires the calling user to have the 'ADMIN' role.
     *
     * @param userId The UUID of the user for whom to revoke tokens.
     * @return A {@link ResponseEntity} indicating success or if the user was not found.
     * @apiNote Requires 'ADMIN' role.
     */
    @PostMapping("/{userId}/revoke-tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> revokeUserTokens(@PathVariable UUID userId) {
        try {
            userService.revokeTokens(userId);
            return ResponseEntity.ok("Tokens revoked for user ID: " + userId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}