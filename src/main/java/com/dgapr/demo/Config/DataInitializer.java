package com.dgapr.demo.Config;


import com.dgapr.demo.Model.Roles;
import com.dgapr.demo.Model.User;
import com.dgapr.demo.Model.UserStatu;
import com.dgapr.demo.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepo,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🔄 Running DataInitializer…");

        String username = "admin";
        if (userRepo.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setEmail("admin@example.com");
            u.setPassword(passwordEncoder.encode("admin"));
            u.setFirstname("System");
            u.setLastname("Administrator");
            u.setIdNumber("0000-ADMIN");
            u.setStatus(UserStatu.ACTIVE);
            u.setRole(Roles.ADMIN);
            u.setCreatedBy("System");
            u.setUpdatedAt(u.getCreatedAt());
            u.setUpdatedBy("System");

            userRepo.save(u);
            log.debug("Creating default user '{}'", username);
            log.info("✅ Created default user '{}'", username);
        } else {
            log.info("ℹ️ Default user '{}' already exists", username);
        }

        log.info("🏁 DataInitializer complete.");
    }
}