package com.dgapr.demo.Audit;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {
    private final String table;
    private final String rowId;
    private final String op;
    private final String details;
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

