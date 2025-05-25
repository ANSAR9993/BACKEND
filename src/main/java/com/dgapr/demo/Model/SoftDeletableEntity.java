package com.dgapr.demo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * An abstract base class for JPA entities that are subject to soft deletion.
 * Entities extending this class will automatically include an 'Is_Deleted' column
 * in their respective database tables. This column is used to logically mark
 * records as deleted rather than physically removing them from the database,
 * which is a common practice in auditing and data retention strategies.
 *
 * <p>The presence of the {@code isDeleted} field allows the {@link com.dgapr.demo.Audit.AuditListener}
 * to differentiate between a standard entity update and a logical soft-delete operation.</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity {
    /**
     * Flag indicating whether the entity is logically deleted.
     * A value of {@code true} means the entity is considered deleted but remains in the database.
     * This field is mapped to the "Is_Deleted" column in the database and defaults to {@code false} (not deleted).
     */
    @Column(name = "Is_Deleted", nullable = false)
    private Boolean isDeleted = false;
}