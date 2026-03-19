package com.anuj.resume_ai_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean mailEnabled;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.mail.from:${MAIL_FROM:}}") String fromAddress,
            @Value("${spring.mail.username:${MAIL_USERNAME:}}") String mailUsername,
            @Value("${spring.mail.password:${MAIL_PASSWORD:}}") String mailPassword,
            @Value("${app.mail.enabled:true}") boolean mailEnabled
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress == null ? "" : fromAddress.trim();

        String normalizedUsername = mailUsername == null ? "" : mailUsername.trim();
        String normalizedPassword = mailPassword == null ? "" : mailPassword.trim();

        this.mailEnabled = mailEnabled
                && !this.fromAddress.isEmpty()
                && !normalizedUsername.isEmpty()
                && !normalizedPassword.isEmpty();
    }

    @Override
    public EmailDeliveryResult sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            logger.warn("Email delivery skipped because no recipient address was provided.");
            return EmailDeliveryResult.skipped("No recipient email address was provided.");
        }

        if (!mailEnabled) {
            logger.warn("Email delivery skipped for {} because the SMTP configuration is incomplete.", to);
            return EmailDeliveryResult.skipped("Email delivery is disabled or missing MAIL_FROM/MAIL_USERNAME/MAIL_PASSWORD.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to.trim());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to {} via SMTP", to);
            return EmailDeliveryResult.sent("Email sent successfully.");
        } catch (MailAuthenticationException e) {
            logger.error("SMTP authentication failed for {}", to, e);
            return EmailDeliveryResult.failed("SMTP authentication failed. For Gmail, use your Gmail address as MAIL_USERNAME and a Google App Password as MAIL_PASSWORD.");
        } catch (MailException e) {
            logger.error("SMTP rejected email for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed via SMTP. Check MAIL_FROM, Gmail App Password, and recipient address.");
        } catch (Exception e) {
            logger.error("Unexpected email failure for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed unexpectedly. Check the Render logs for the exact error.");
        }
    }
}
