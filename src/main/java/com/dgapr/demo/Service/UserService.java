package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.UserDto.UserDto;
import com.dgapr.demo.Dto.UserDto.UserResponseDto;
import com.dgapr.demo.Model.User;
import com.dgapr.demo.Model.UserStatu;
import com.dgapr.demo.Repository.UserRepository;
import com.dgapr.demo.Specification.UserSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponseDto> getUsers(Pageable pageable,
                                          Map<String,String> filterParams) {
        UserSpecification spec = new UserSpecification(filterParams);
        Page<User> page = userRepository.findAll(spec, pageable);
        return page.map(u -> modelMapper.map(u, UserResponseDto.class));
    }
//
//    public List<UserResponseDto> getAllUsers() {
//        return userRepository.findAll()
//                .stream()
//                .map(user -> modelMapper.map(user, UserResponseDto.class))
//                .collect(Collectors.toList());
//    }

    public UserResponseDto getUserById(UUID id) throws EntityNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.debug("Creating user with username: {}", userDto.getUsername());
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        Map<String, String> validationErrors = new java.util.HashMap<>();
        if (userRepository.existsByUsername(userDto.getUsername().trim().toLowerCase())) {
            validationErrors.put("username", "Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail().trim().toLowerCase())) {
            log.debug("Email already exists: {}", userDto.getEmail());
            validationErrors.put("email", "Email already exists");
        }
        if (userRepository.existsByIdNumber(userDto.getIdNumber().trim().toLowerCase())) {
            validationErrors.put("idNumber", "ID number already exists");
        }
        if (!validationErrors.isEmpty()) {
            throw new com.dgapr.demo.Exception.MultiFieldValidationException(
                "Validation failed due to duplicate fields", validationErrors);
        }
        User user = new User();
        updateUserFromDto(user, userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword().trim()));
        log.debug("Saving new user: {}", user);
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        log.debug("Updating user {} with data: {}", id, userDto);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Map<String, String> validationErrors = new java.util.HashMap<>();
        if (!user.getUsername().equals(userDto.getUsername()) &&
                userRepository.existsByUsername(userDto.getUsername().trim().toLowerCase())) {
            validationErrors.put("username", "Username already exists");
        }
        if (!user.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail().trim().toLowerCase())) {
            validationErrors.put("email", "Email already exists");
        }
        if (!user.getIdNumber().equals(userDto.getIdNumber()) &&
                userRepository.existsByIdNumber(userDto.getIdNumber().trim().toLowerCase())) {
            validationErrors.put("idNumber", "ID number already exists");
        }
        if (!validationErrors.isEmpty()) {
            throw new com.dgapr.demo.Exception.MultiFieldValidationException(
                "Validation failed due to duplicate fields", validationErrors);
        }
        updateUserFromDto(user, userDto);
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setTokenVersion(user.getTokenVersion() + 1);
            log.info("Password changed for user {}. Token version incremented to {}.", user.getUsername(), user.getTokenVersion());
        }
        User updatedUser = userRepository.save(user);
        log.debug("Saving updated user: {}", updatedUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setIsDeleted(true);
        user.setStatus(UserStatu.DELETED);
        userRepository.save(user);
    }

    @Transactional
    public void revokeTokens(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID " + userId));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    private void updateUserFromDto(User user, UserDto userDto) {
        user.setUsername(userDto.getUsername().trim().toLowerCase());
        user.setIdNumber(userDto.getIdNumber().trim().toLowerCase());
        user.setFirstname(userDto.getFirstname().trim());
        user.setLastname(userDto.getLastname().trim());
        user.setEmail(userDto.getEmail().trim().toLowerCase());
        user.setRole(userDto.getRole());

        if (userDto.getStatus() == null) {
            user.setStatus(UserStatu.ACTIVE);
            user.setIsDeleted(false);
        } else {
            user.setStatus(userDto.getStatus());
            if (userDto.getStatus() == UserStatu.DELETED) {
                user.setIsDeleted(true);
            } else {
                user.setIsDeleted(false);
            }
        }
    }

    public UserResponseDto loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
        return modelMapper.map(user, UserResponseDto.class);
    }
}
