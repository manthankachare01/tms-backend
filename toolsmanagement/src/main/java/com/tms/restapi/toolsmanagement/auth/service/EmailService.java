package com.tms.restapi.toolsmanagement.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendOtp(String to, String otp) {
        if (mailSender == null) {
            logger.warn("Mail sender not configured. OTP for {}: {}", to, otp);
            throw new RuntimeException("Mail sender not configured. For dev, check logs for OTP.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your password reset OTP");
            message.setText("Your OTP for password reset is: " + otp + "\nThis OTP will expire in 5 minutes.");
            mailSender.send(message);
            logger.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            logger.warn("Failed to send OTP email to {}. OTP: {}. Error: {}", to, otp, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. For dev, check logs for OTP: " + otp, e);
        }
    }
}
