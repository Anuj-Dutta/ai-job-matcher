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
            @Value("${app.mail.from:${spring.mail.username:}}") String fromAddress,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${app.mail.enabled:true}") boolean mailEnabled
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress == null ? "" : fromAddress.trim();
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        this.mailEnabled = mailEnabled
                && !normalizedUsername.isEmpty()
                && !normalizedPassword.isEmpty()
                && !"YOUR_APP_PASSWORD".equalsIgnoreCase(normalizedPassword);
    }

    @Override
    public EmailDeliveryResult sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            logger.warn("Email delivery skipped because no recipient address was provided.");
            return EmailDeliveryResult.skipped("No recipient email address was provided.");
        }

        if (!mailEnabled) {
            logger.warn("Email delivery skipped for {} because mail delivery is disabled or incomplete.", to);
            return EmailDeliveryResult.skipped("Email delivery is disabled or incomplete on the server.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
            return EmailDeliveryResult.sent("Email sent successfully.");
        } catch (MailAuthenticationException e) {
            logger.error("SMTP authentication failed while sending email to {}", to, e);
            return EmailDeliveryResult.failed("SMTP authentication failed. Verify MAIL_USERNAME and use a Gmail App Password in MAIL_PASSWORD.");
        } catch (MailException e) {
            logger.error("Mail transport failed for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed at the SMTP server. Check the Render logs for the exact mail error.");
        } catch (Exception e) {
            logger.error("Unexpected email failure for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed unexpectedly. Check the Render logs for the exact error.");
        }
    }
}
