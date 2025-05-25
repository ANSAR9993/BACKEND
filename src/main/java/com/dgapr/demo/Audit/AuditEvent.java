package com.dgapr.demo.Audit;

import com.dgapr.demo.Model.AuditLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Custom application event fired when an auditable operation occurs on an entity.
 * This event carries the necessary data to create an {@link AuditLog} entry.
 *
 * <p>It extends Spring's {@link ApplicationEvent} and uses Lombok's {@code @Getter}
 * to automatically generate getter methods for its fields.</p>
 */
@Getter
public class AuditEvent extends ApplicationEvent {

    /** The name of the database table affected by the audit event. */
    private final String table;
    /** The ID of the specific row (entity) in the table that was affected. */
    private final String rowId;
    /** The type of operation that occurred (e.g., "CREATE", "UPDATE", "DELETE"). */
    private final String op;
    /** Additional details about the operation, often a JSON string representing changes. */
    private final String details;
    /** The principal (username) who modified the entity. */
    private final String modifiedBy;

    public AuditEvent(Object source, String table, String rowId, String op, String details, String modifiedBy) {
        super(source);
        this.table = table;
        this.rowId = rowId;
        this.op = op;
        this.details = details;
        this.modifiedBy = modifiedBy;
    }

}

