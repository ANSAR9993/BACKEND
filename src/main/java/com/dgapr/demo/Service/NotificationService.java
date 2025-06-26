package com.dgapr.demo.Service;

import com.dgapr.demo.Model.Certificate.Certificate;
import com.dgapr.demo.Model.Certificate.CertificateNotification;
import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Model.User.User;
import com.dgapr.demo.Model.User.UserStatu;
import com.dgapr.demo.Repository.CertifRepository;
import com.dgapr.demo.Repository.CertificateNotificationRepository;
import com.dgapr.demo.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for sending notifications about certificate expirations to administrators.
 * <p>
 * This service checks for certificates that are about to expire or have already expired, sends notification emails
 * to admin users, and records notification events to prevent duplicate alerts. The notification process is scheduled
 * to run automatically at a specified interval.
 * </p>
 *
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Identify certificates expiring soon or already expired</li>
 *   <li>Send notification emails to admin and super admin users</li>
 *   <li>Record sent notifications to avoid duplicate alerts</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final CertifRepository certifRepository;
    private final CertificateNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private static final String BEFORE_EXPIRY = "BEFORE_EXPIRY";
    private static final String AFTER_EXPIRY = "AFTER_EXPIRY";

    /**
     * Scheduled task that checks for certificates expiring within 30 days or already expired,
     * sends notification emails to admin users, and records the notifications.
     *
     * The method is triggered automatically based on the defined cron expression.
     */
    @Scheduled(cron = "0 54 21 ? * TUE")
    @Transactional
    public void sendCertificateExpiryNotifications() {
        log.info("Scheduled notification task triggered");
        List<String> adminEmails = userRepository.findAll().stream()
                .filter(u -> (u.getRole() == Role.ADMIN || u.getRole() == Role.SUPER_ADMIN)
                        && u.getStatus() == UserStatu.ACTIVE)
                .map(User::getEmail)
                .distinct()
                .collect(Collectors.toList());
        if (adminEmails.isEmpty()) return;

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);

        List<Certificate> expiringSoon = certifRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .filter(c -> c.getExpirationDate() != null)
                .filter(c -> {
                    LocalDate exp = c.getExpirationDate();
                    return exp.isAfter(today) && !exp.isAfter(in30Days)
                            && !notificationRepository.existsByCertificateIdAndNotificationType(c.getId(), BEFORE_EXPIRY);
                })
                .toList();

        List<Certificate> expired = certifRepository.findAll().stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .filter(c -> c.getExpirationDate() != null)
                .filter(c -> {
                    LocalDate exp = c.getExpirationDate();
                    return exp.isBefore(today)
                            && !notificationRepository.existsByCertificateIdAndNotificationType(c.getId(), AFTER_EXPIRY);
                })
                .toList();

        if (expiringSoon.isEmpty() && expired.isEmpty()) return;

        StringBuilder sb = getStringBuilder(expiringSoon, expired);

        sendEmailAsync(adminEmails, "[Notification] Certificats expirés ou en cours d'expiration", sb.toString());

        // Record notifications
        LocalDate now = LocalDate.now();
        for (Certificate c : expiringSoon) {
            CertificateNotification notif = new CertificateNotification();
            notif.setCertificateId(c.getId());
            notif.setNotificationType(BEFORE_EXPIRY);
            notif.setNotifiedAt(now);
            notificationRepository.save(notif);
        }
        for (Certificate c : expired) {
            CertificateNotification notif = new CertificateNotification();
            notif.setCertificateId(c.getId());
            notif.setNotificationType(AFTER_EXPIRY);
            notif.setNotifiedAt(now);
            notificationRepository.save(notif);
        }
    }

    /**
     * Builds the email body listing certificates that are expiring soon or already expired.
     *
     * @param expiringSoon List of certificates expiring within 30 days
     * @param expired List of certificates already expired
     * @return StringBuilder containing the formatted email body
     */
    private static StringBuilder getStringBuilder(List<Certificate> expiringSoon, List<Certificate> expired) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour,\n\nVoici la liste des certificats concernés :\n\n");
        if (!expiringSoon.isEmpty()) {
            sb.append("Certificats expirant dans 30 jours ou moins :\n");
            for (Certificate c : expiringSoon) {
                sb.append(String.format("- %s (ID Demand: %s), expire le %s\n", c.getCommonName(), c.getIdDemand(), c.getExpirationDate()));
            }
            sb.append("\n");
        }
        if (!expired.isEmpty()) {
            sb.append("Certificats déjà expirés :\n");
            for (Certificate c : expired) {
                sb.append(String.format("- %s (ID Demand: %s), expire le %s\n", c.getCommonName(), c.getIdDemand(), c.getExpirationDate()));
            }
            sb.append("\n");
        }
        sb.append("Merci de prendre les mesures nécessaires.\n\nCeci est un message automatique.");
        return sb;
    }

    /**
     * Sends an email asynchronously to the specified recipients.
     *
     * @param recipients List of recipient email addresses
     * @param subject Email subject
     * @param body Email body content
     */
    @Async
    public void sendEmailAsync(List<String> recipients, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipients.toArray(new String[0]));
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Notification email sent to admins");
        } catch (Exception e) {
            log.error("Failed to send notification email", e);
        }
    }
}

