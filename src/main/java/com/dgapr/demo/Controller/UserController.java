package com.dgapr.demo.Controller;

import com.dgapr.demo.Dto.UserDto.UserDto;
import com.dgapr.demo.Dto.UserDto.UserResponseDto;
import com.dgapr.demo.Model.User;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam Map<String,String> filterParams
    ) {
        Page<UserResponseDto> result = userService.getUsers(pageable, filterParams);
        return ResponseEntity.ok(result);
    }

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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
        try {
            log.debug("Updating user {} with data: {}", id, userDto);
            UserDto user = userService.updateUser(id, userDto);
            log.debug("User updated successfully: {}", user);
            return ResponseEntity.ok(Map.of(
                    "message", String.format("User '%s' (ID-NUMBER: %s) has been successfully updated", user.getUsername(), user.getIdNumber()),
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
                    "User '%s' (ID-NUMBER: %s) has been successfully deleted",
                    user.getUsername(),
                    user.getIdNumber()
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

}
