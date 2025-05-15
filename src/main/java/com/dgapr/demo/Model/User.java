package com.dgapr.demo.Model;

import com.dgapr.demo.Audit.AuditListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE users SET Is_Deleted = true WHERE id = ?") // New annotation
@SQLRestriction("Is_Deleted = false")
@Table(name = "users")
@EntityListeners(AuditListener.class)
public class User extends AuditedEntity implements Identifiable<UUID> {

    @Id
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(length = 50, unique = true, nullable = false)
    private String username;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name",nullable = false)
    private String lastname;

    @Column(name = "id_number", unique = true, nullable = false)
    private String idNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatu status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by")
    private String updatedBy;

    @Override
    public UUID getId() {
        return id;
    }

    @PrePersist
    public void generateId(){
        if(id == null){
            id = UUID.randomUUID();
        }
    }


}
