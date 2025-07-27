package com.krainet.auth.controller;

import com.krainet.auth.dto.NotificationDTO;
import com.krainet.auth.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationDTO notification) {
        try {
            emailService.sendEmail(notification);
            return ResponseEntity.ok().build();
        } catch (MessagingException e) {
            logger.error("Failed to send email: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
