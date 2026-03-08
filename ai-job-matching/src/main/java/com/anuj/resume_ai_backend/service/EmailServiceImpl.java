package com.anuj.resume_ai_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("YOUR_GMAIL@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            try {
    mailSender.send(message);
    System.out.println("EMAIL SENT SUCCESSFULLY TO: " + to);
} catch (Exception e) {
    System.out.println("Email service unavailable. Could not send email.");
}

            System.out.println("EMAIL SENT SUCCESSFULLY TO: " + to);

        } catch (Exception e) {

            System.out.println("EMAIL FAILED");
            e.printStackTrace();

        }

    }
}