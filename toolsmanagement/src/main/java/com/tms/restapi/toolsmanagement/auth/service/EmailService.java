package com.tms.restapi.toolsmanagement.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Brevo config (set as env vars or in application.properties)
    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:no-reply@example.com}")
    private String brevoSenderEmail;

    @Value("${brevo.sender.name:ToolsManagement}")
    private String brevoSenderName;

    private final RestTemplate rest = new RestTemplate();

    public void sendOtp(String to, String otp) {
        // Prefer Brevo API if key provided (useful on platforms where SMTP is blocked)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", to);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Your password reset OTP");
                payload.put("textContent", "Your OTP for password reset is: " + otp + "\nThis OTP will expire in 10 minutes.");

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> resp = rest.postForEntity(url, entity, String.class);
                logger.info("Brevo response: {}", resp.getStatusCode());
                return;
            } catch (Exception e) {
                logger.warn("Brevo send failed for {} otp {}: {}", to, otp, e.getMessage());
                throw new RuntimeException("Brevo send failed. Check logs for OTP: " + otp, e);
            }
        }

        // Fallback to JavaMailSender if available
        if (mailSender == null) {
            logger.warn("Mail sender not configured and Brevo API key not set. OTP for {}: {}", to, otp);
            throw new RuntimeException("Mail sender not configured and Brevo not configured. For dev, check logs for OTP.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your password reset OTP");
            message.setText("Your OTP for password reset is: " + otp + "\nThis OTP will expire in 10 minutes.");
            mailSender.send(message);
            logger.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            logger.warn("Failed to send OTP email to {}. OTP: {}. Error: {}", to, otp, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. For dev, check logs for OTP: " + otp, e);
        }
    }
}
