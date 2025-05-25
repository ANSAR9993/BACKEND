package com.dgapr.demo.Model.Certificate;

import com.dgapr.demo.Audit.AuditListener;
import com.dgapr.demo.Model.Identifiable;
import com.dgapr.demo.Model.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/**
 * Entity representing a notification sent regarding a certificate's expiration status.
 * Ensures uniqueness for each certificate and notification type combination.
 * Stores the date when the notification was sent.
 */
@Getter
@Setter
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "certificate_notification",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"certificate_id", "notification_type"})})
public class CertificateNotification extends SoftDeletableEntity implements Identifiable<Long> {
    /**
     * Unique identifier for the notification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The ID of the certificate related to this notification.
     */
    @Column(name = "certificate_id", nullable = false)
    private Long certificateId;

    /**
     * The type of notification (e.g., "BEFORE_EXPIRY", "AFTER_EXPIRY").
     */
    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    /**
     * The date when the notification was sent.
     */
    @Column(name = "notified_at", nullable = false)
    private LocalDate notifiedAt;

    /**
     * Gets the unique identifier for this notification.
     * @return the notification ID
     */
    @Override
    public Long getId() {
        return id;
    }
}

