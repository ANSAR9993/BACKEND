package com.dgapr.demo.Repository;

import com.dgapr.demo.Model.CertificateNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateNotificationRepository extends JpaRepository<CertificateNotification, Long> {
    boolean existsByCertificateIdAndNotificationType(Long certificateId, String notificationType);
}

