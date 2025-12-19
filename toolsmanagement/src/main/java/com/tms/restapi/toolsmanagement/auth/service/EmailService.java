package com.tms.restapi.toolsmanagement.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendOtp(String to, String otp) {
        if (mailSender == null) {
            // Mail not configured; in dev return (or log). For now, throw runtime to notify.
            throw new RuntimeException("Mail sender not configured. Set SMTP properties in application.properties");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your password reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\nThis OTP will expire in 5 minutes.");
        mailSender.send(message);
    }
}
