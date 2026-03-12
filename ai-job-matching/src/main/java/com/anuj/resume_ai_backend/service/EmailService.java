package com.anuj.resume_ai_backend.service;

public interface EmailService {

    EmailDeliveryResult sendEmail(String to, String subject, String body);
}
