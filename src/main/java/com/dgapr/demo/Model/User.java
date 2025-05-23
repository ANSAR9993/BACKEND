package com.dgapr.demo.Model;

import com.dgapr.demo.Audit.AuditListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE users SET Is_Deleted = 1 WHERE id = ?")
@Table(name = "users")
@EntityListeners({
        AuditListener.class,
        AuditingEntityListener.class
})
public class User extends AuditedEntity implements Identifiable<UUID>, UserDetails {

    @Id
    @Column(columnDefinition = "uniqueidentifier", updatable = false)
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
    private Role role; // USER, ADMIN, SUPER_ADMIN

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatu status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(nullable = false)
    private Long tokenVersion = 0L;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return status != UserStatu.SUSPENDED || status != UserStatu.DELETED; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return status == UserStatu.ACTIVE; }

}
