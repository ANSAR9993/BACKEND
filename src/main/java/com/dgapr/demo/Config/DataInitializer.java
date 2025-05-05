package com.dgapr.demo.Config;


import com.dgapr.demo.Model.Role;
import com.dgapr.demo.Model.User;
import com.dgapr.demo.Model.UserStatu;
import com.dgapr.demo.Repository.RoleRepository;
import com.dgapr.demo.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🔄 Running DataInitializer…");

        Role userRole  = createRoleIfNotFound("USER",  "Standard User");
        Role adminRole = createRoleIfNotFound("ADMIN", "Administrator");

        String username = "admin";
        if (userRepo.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setEmail("admin@example.com");
            u.setPasswordHash(passwordEncoder.encode("admin"));
            u.setFirstname("System");
            u.setLastname("Administrator");
            u.setIdNumber("0000-ADMIN");
            u.setStatus(UserStatu.ACTIVE);

            u.setRoles(Set.of(userRole, adminRole));

            userRepo.save(u);
            log.info("✅ Created default user '{}'", username);
        } else {
            log.info("ℹ️ Default user '{}' already exists", username);
        }

        log.info("🏁 DataInitializer complete.");
    }

    private Role createRoleIfNotFound(String name, String desc) {
        return roleRepo.findByName(name)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(name);
                    r.setDescription(desc);
                    log.info("➕ Creating role '{}'", name);
                    return roleRepo.save(r);
                });
    }
}