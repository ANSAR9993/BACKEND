package com.dgapr.demo.Model;

import com.dgapr.demo.Audit.AuditListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "certificate_notification",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"certificate_id", "notification_type"})})
public class CertificateNotification extends AuditedEntity implements Identifiable<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_id", nullable = false)
    private Long certificateId;

    @Column(name = "notification_type", nullable = false)
    private String notificationType; // "BEFORE_EXPIRY" or "AFTER_EXPIRY"

    @Column(name = "notified_at", nullable = false)
    private LocalDate notifiedAt;

    @Override
    public Long getId() {
        return id;
    }
}

