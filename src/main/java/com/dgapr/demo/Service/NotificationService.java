package com.dgapr.demo.Service;

import com.dgapr.demo.Model.Certificate;
import com.dgapr.demo.Model.CertificateNotification;
import com.dgapr.demo.Model.Role;
import com.dgapr.demo.Model.User;
import com.dgapr.demo.Model.UserStatu;
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

    @Scheduled(cron = "0 58 13 ? * WED")
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

    private static StringBuilder getStringBuilder(List<Certificate> expiringSoon, List<Certificate> expired) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour,\n\nVoici la liste des certificats concernés :\n\n");
        if (!expiringSoon.isEmpty()) {
            sb.append("Certificats expirant dans 30 jours ou moins :\n");
            for (Certificate c : expiringSoon) {
                sb.append(String.format("- %s (ID: %s), expire le %s\n", c.getDemandeName(), c.getIdDemand(), c.getExpirationDate()));
            }
            sb.append("\n");
        }
        if (!expired.isEmpty()) {
            sb.append("Certificats déjà expirés :\n");
            for (Certificate c : expired) {
                sb.append(String.format("- %s (ID: %s), expiré le %s\n", c.getDemandeName(), c.getIdDemand(), c.getExpirationDate()));
            }
            sb.append("\n");
        }
        sb.append("Merci de prendre les mesures nécessaires.\n\nCeci est un message automatique.");
        return sb;
    }

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

