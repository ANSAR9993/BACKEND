package com.dgapr.demo.Config;
/*
import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Model.User.User;
import com.dgapr.demo.Model.User.UserStatu;
import com.dgapr.demo.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

@Component
@Slf4j*/
public class DataInitializer /*implements CommandLineRunner*/ {

//    private final UserRepository userRepo;
//    private final PasswordEncoder passwordEncoder;
//
//    public DataInitializer(UserRepository userRepo,
//                           PasswordEncoder passwordEncoder) {
//        this.userRepo = userRepo;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        log.info("Running DataInitializer…");
//
//        String username = "user";
//        if (userRepo.findByUsername(username).isEmpty()) {
//            log.info("About to create user with username: {}", username);
//            User u = new User();
//            u.setUsername(username);
//            u.setEmail("user@example.com");
//            u.setPassword(passwordEncoder.encode("user"));
//            u.setFirstname("System");
//            u.setLastname("Administrator");
//            u.setIdNumber("0100-user");
//            u.setStatus(UserStatu.ACTIVE);
//            u.setRole(Role.USER);
//            u.setCreatedBy("System");
//            u.setUpdatedAt(u.getCreatedAt());
//            u.setUpdatedBy("System");
//
//            userRepo.save(u);
//            log.info("userRepo.save(u) called for user with id: {}", u.getId());
//            log.debug("Creating default user '{}'", username);
//            log.info("Created default user '{}'", username);
//        } else {
//            log.info("ℹDefault user '{}' already exists", username);
//        }
//
//        log.info("DataInitializer complete.");
//    }
}

