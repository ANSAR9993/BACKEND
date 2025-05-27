package com.dgapr.demo.Model.User;

import com.dgapr.demo.Audit.AuditListener;
import com.dgapr.demo.Model.Identifiable;
import com.dgapr.demo.Model.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
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

/**
 * Represents an application user, with auditing and soft-delete support.
 * Implements Spring Security's UserDetails so it can be returned directly
 * by CustomUserDetailsService.
 */
/**
 * Represents an application user, with auditing and soft-delete support.
 * Implements Spring Security's UserDetails so it can be returned directly
 * by CustomUserDetailsService.
 *
 * <p>This entity is mapped to the "users" table in the database.</p>
 *
 * <p>Soft-deletion is implemented via {@code @SQLDelete}, marking a user as deleted
 * rather than physically removing them from the database.</p>
 *
 * <p>Auditing fields ({@code createdAt}, {@code createdBy}, {@code updatedAt}, {@code updatedBy})
 * are automatically managed by Spring Data JPA's {@link AuditingEntityListener}
 * and a custom {@link AuditListener}.</p>
 */
@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE users SET Is_Deleted = 1 WHERE id = ?")
@Table(name = "users")
@EntityListeners({
        AuditListener.class,
        AuditingEntityListener.class
})
//@EntityListeners(AuditListener.class)
public class User extends SoftDeletableEntity implements Identifiable<UUID>, UserDetails {

    /** Primary key; generated as a UUID before insert if not set. */
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

    /** Roles determine authorities (USER, ADMIN, SUPER_ADMIN). */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private com.dgapr.demo.Model.User.Role role;

    /** Status controls account locking/enabling (ACTIVE, SUSPENDED, DELETED). */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private com.dgapr.demo.Model.User.UserStatu status;

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

    /** Used to invalidate issued tokens by incrementing after logout/password reset. */
    @Column(nullable = false)
    private Long tokenVersion = 0L;

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Ensure a UUID is generated if none is provided.
     */
    @PrePersist
    public void generateId(){
        if(id == null){
            id = UUID.randomUUID();
        }
    }

    /**
     * Grant the Spring Security authority based on the Role enum.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return password; }

    @Override public String getUsername() { return username; }

    @Override public boolean isAccountNonExpired() { return true; }

    /**
     * An account is non-locked if its status is neither SUSPENDED nor DELETED.
     *
     * @return true if status != SUSPENDED && status != DELETED
     */
    @Override public boolean isAccountNonLocked() { return status != com.dgapr.demo.Model.User.UserStatu.SUSPENDED; }

    @Override public boolean isCredentialsNonExpired() { return true; }

    /**
     * An account is enabled only when status == ACTIVE.
     */
    @Override public boolean isEnabled() { return status == com.dgapr.demo.Model.User.UserStatu.ACTIVE; }

}
