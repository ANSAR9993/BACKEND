// src/main/java/com/dgapr/demo/Model/AuditLog.java
package com.dgapr.demo.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@Entity
@Table(
        name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_table_ts",      columnList = "tableName, timestamp"),
                @Index(name = "idx_audit_rowid",         columnList = "rowId"),
                @Index(name = "idx_audit_modified_by",   columnList = "modifiedBy")
        }
)
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private String rowId;

    @Column(nullable = false)
    private String operation;

    @Column(nullable = false)
    private String modifiedBy;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 2000)
    private String details;
}
