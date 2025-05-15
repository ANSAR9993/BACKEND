package com.dgapr.demo.Service;

import com.dgapr.demo.Dto.UserDto.UserDto;
import com.dgapr.demo.Dto.UserDto.UserResponseDto;
import com.dgapr.demo.Model.User;
import com.dgapr.demo.Model.UserStatu;
import com.dgapr.demo.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDto> getUserById(UUID id) throws EntityNotFoundException {

        return userRepository.findById(id)
                .stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .findFirst();

    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.debug("Creating user with username: {}", userDto.getUsername());
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByIdNumber(userDto.getIdNumber())) {
            throw new IllegalArgumentException("ID number already exists");
        }

        User user = new User();
        updateUserFromDto(user, userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        log.debug("Saving new user: {}", user);

        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        log.debug("Updating user {} with data: {}", id, userDto);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.getUsername().equals(userDto.getUsername()) &&
                userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!user.getEmail().equals(userDto.getEmail()) &&
                userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!user.getIdNumber().equals(userDto.getIdNumber()) &&
                userRepository.existsByIdNumber(userDto.getIdNumber())) {
            throw new IllegalArgumentException("ID number already exists");
        }

        updateUserFromDto(user, userDto);
        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        log.debug("Saving updated user: {}", user);
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private void updateUserFromDto(User user, UserDto userDto) {
        user.setUsername(userDto.getUsername());
        user.setIdNumber(userDto.getIdNumber());
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());

        // Set default status to ACTIVE if not specified
        if (userDto.getStatus() == null) {
            user.setStatus(UserStatu.ACTIVE);
        } else {
            user.setStatus(UserStatu.valueOf(userDto.getStatus().name()));
        }
    }

    public UserResponseDto loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return modelMapper.map(userRepository.findByUsername(username), UserResponseDto.class);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User with username " + username + " not found", e);
        }
    }
}
