package com.dgapr.demo.Repository;

import com.dgapr.demo.Model.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import this

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities, exposing standard CRUD operations
 * plus JPA Specifications for dynamic queries.
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByIdNumber(String idNumber);
    Optional<User> findByUsername(String username);
}