package com.dgapr.demo.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a single entry in the application's audit log.
 * This entity stores detailed information about operations performed on other entities,
 * including the table name, the ID of the affected row, the type of operation,
 * who performed it, when it occurred, and any relevant details (changes old/new values).
 *
 * <p>This entity is mapped to the "audit_log" table in the database and includes
 * various indexes to optimize query performance for audit trail analysis.</p>
 */
@Getter
@Setter
@Entity
@Table(
        name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_table_rowid",   columnList = "tableName, rowId"),
                @Index(name = "idx_audit_table_ts",      columnList = "tableName, timestamp"),
                @Index(name = "idx_audit_rowid",         columnList = "rowId"),
                @Index(name = "idx_audit_modified_by",   columnList = "modifiedBy")
        }
)
public class AuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    /**
     * The name of the database table on which the audited operation occurred.
     */
    @Column(nullable = false)
    private String tableName;

    /**
     * The ID of the specific row (entity) in the {@link #tableName} that was affected by the operation.
     * Stored as a String to accommodate various ID types (e.g., UUID, Long).
     */
    @Column(nullable = false)
    private String rowId;

    /**
     * The type of operation performed (e.g., "CREATE", "UPDATE", "DELETE", "HARD_DELETE").
     */
    @Column(nullable = false)
    private String operation;

    /**
     * The principal (username) who initiated the audited operation.
     */
    @Column(nullable = false)
    private String modifiedBy;

    /**
     * The timestamp (in UTC) when the operation occurred.
     */
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Additional details about the audited operation, typically a JSON string.
     * For "UPDATE" operations, this might contain a diff of old and new values.
     * Limited to 2000 characters.
     */
    @Column(length = 2000)
    private String details;
}
