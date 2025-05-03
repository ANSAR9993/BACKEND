//package com.dgapr.demo.Config;
//
//
//import com.dgapr.demo.Entity.Role;
//import com.dgapr.demo.Entity.User;
//import com.dgapr.demo.Repository.RoleRepository;
//import com.dgapr.demo.Repository.UserRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional; // Import Transactional
//
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.Set;
//
//@Component
//@Slf4j
//public class DataInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public DataInitializer(UserRepository userRepository,
//                           RoleRepository roleRepository,
//                           PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.roleRepository = roleRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    @Transactional // Important for managing lazy loading or multiple saves
//    public void run(String... args) throws Exception {
//        log.info("Running Data Initializer...");
//
//        // --- Create Roles if they don't exist ---
//        Role userRole = createRoleIfNotFound("USER", "Standard User Role");
//        Role adminRole = createRoleIfNotFound("ADMIN", "Administrator Role"); // Uncomment if you need an ADMIN role
//
//        // --- Create Test User if it doesn't exist ---
//        String testUsername = "admin";
//        if (userRepository.findByUsername(testUsername).isEmpty()) {
//            User testUser = new User();
//            testUser.setUsername(testUsername);
//            testUser.setEmail("admin@example.com"); // Use a unique email
//            // Encode the password before saving!
//            testUser.setPasswordHash(passwordEncoder.encode("admin"));
//
//            Set<Role> roles = new HashSet<>();
//            roles.add(userRole);
//            roles.add(adminRole); // Add admin role if needed
//
//            testUser.setRoles(roles);
//            userRepository.save(testUser);
//            log.info("Created test user: {}", testUsername);
//        } else {
//            log.info("Test user '{}' already exists.", testUsername);
//        }
//
//        log.info("Data Initializer finished.");
//    }
//
//    // Helper method to avoid duplicate role creation
//    private Role createRoleIfNotFound(String name, String description) {
//        Optional<Role> roleOptional = roleRepository.findByName(name);
//        if (roleOptional.isEmpty()) {
//            Role newRole = new Role();
//            newRole.setName(name);
//            newRole.setDescription(description);
//            log.info("Creating role: {}", name);
//            return roleRepository.save(newRole);
//        } else {
//            log.info("Role '{}' already exists.", name);
//            return roleOptional.get();
//        }
//    }
//}
