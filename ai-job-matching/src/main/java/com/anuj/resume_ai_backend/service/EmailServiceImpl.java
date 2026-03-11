package com.anuj.resume_ai_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean mailConfigured;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress,
            @Value("${spring.mail.password:}") String password
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress == null ? "" : fromAddress.trim();

        String normalizedPassword = password == null ? "" : password.trim();
        this.mailConfigured = !this.fromAddress.isEmpty()
                && !normalizedPassword.isEmpty()
                && !"YOUR_APP_PASSWORD".equalsIgnoreCase(normalizedPassword);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (!mailConfigured) {
            System.out.println("Email not configured. Skipping send to: " + to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.out.println("Email send failed for: " + to);
            e.printStackTrace();
        }
    }
}
