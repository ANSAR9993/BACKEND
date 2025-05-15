package com.dgapr.demo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditedEntity {
    @Column(name = "Is_Deleted", nullable = false)
    private Boolean isDeleted = false;
}
