package com.anuj.resume_ai_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final String fromAddress;
    private final String provider;
    private final String brevoApiKey;
    private final String brevoBaseUrl;
    private final boolean mailEnabled;
    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public EmailServiceImpl(
            ObjectMapper objectMapper,
            @Value("${app.mail.from:${MAIL_FROM:}}") String fromAddress,
            @Value("${app.mail.provider:${MAIL_PROVIDER:brevo}}") String provider,
            @Value("${app.mail.brevo.api-key:${BREVO_API_KEY:}}") String brevoApiKey,
            @Value("${app.mail.brevo.base-url:${BREVO_BASE_URL:https://api.brevo.com/v3/smtp/email}}") String brevoBaseUrl,
            @Value("${app.mail.connection-timeout-ms:${MAIL_CONNECTION_TIMEOUT_MS:5000}}") long connectionTimeoutMs,
            @Value("${app.mail.read-timeout-ms:${MAIL_READ_TIMEOUT_MS:10000}}") long readTimeoutMs,
            @Value("${app.mail.enabled:true}") boolean mailEnabled
    ) {
        this.objectMapper = objectMapper;
        this.fromAddress = fromAddress == null ? "" : fromAddress.trim();
        this.provider = provider == null ? "" : provider.trim().toLowerCase();
        this.brevoApiKey = brevoApiKey == null ? "" : brevoApiKey.trim();
        this.brevoBaseUrl = brevoBaseUrl == null ? "" : brevoBaseUrl.trim();
        this.mailEnabled = mailEnabled
                && !this.fromAddress.isEmpty()
                && "brevo".equals(this.provider)
                && !this.brevoApiKey.isEmpty();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectionTimeoutMs))
                .build();
        this.requestTimeout = Duration.ofMillis(readTimeoutMs);
    }

    @Override
    public EmailDeliveryResult sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            logger.warn("Email delivery skipped because no recipient address was provided.");
            return EmailDeliveryResult.skipped("No recipient email address was provided.");
        }

        if (!mailEnabled) {
            logger.warn("Email delivery skipped for {} because the Brevo configuration is incomplete.", to);
            return EmailDeliveryResult.skipped("Email delivery is disabled or missing MAIL_FROM/BREVO_API_KEY.");
        }

        try {
            Sender sender = parseSender(fromAddress);
            String payload = objectMapper.writeValueAsString(Map.of(
                    "sender", Map.of("name", sender.name(), "email", sender.email()),
                    "to", List.of(Map.of("email", to.trim())),
                    "subject", subject,
                    "textContent", body
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(brevoBaseUrl))
                    .timeout(requestTimeout)
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
                logger.info("Email sent successfully to {} via Brevo HTTP API", to);
                return EmailDeliveryResult.sent("Email sent successfully.");
            }

            logger.error("Brevo email delivery failed for {} with status {} and body {}", to, response.statusCode(), truncate(response.body()));
            return EmailDeliveryResult.failed(buildBrevoFailureMessage(response.statusCode(), response.body()));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize Brevo email payload for {}", to, e);
            return EmailDeliveryResult.failed("Email payload could not be created. Check the sender address and email content.");
        } catch (IllegalArgumentException e) {
            logger.error("Brevo email configuration is invalid for {}", to, e);
            return EmailDeliveryResult.failed(e.getMessage());
        } catch (java.net.http.HttpTimeoutException | ConnectException e) {
            logger.error("Brevo API request timed out for {}", to, e);
            return EmailDeliveryResult.failed("Brevo API request timed out. Check outbound HTTPS access from the running environment.");
        } catch (UnknownHostException e) {
            logger.error("Brevo API host could not be resolved for {}", to, e);
            return EmailDeliveryResult.failed("Brevo API host could not be resolved. Check BREVO_BASE_URL and DNS/network access.");
        } catch (IOException e) {
            logger.error("Brevo API request failed for {}", to, e);
            return EmailDeliveryResult.failed("Brevo API request failed. Check BREVO_API_KEY, BREVO_BASE_URL, and outbound HTTPS access.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Brevo API request was interrupted for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery was interrupted before Brevo could respond.");
        } catch (Exception e) {
            logger.error("Unexpected email failure for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed unexpectedly. Check the application logs for the exact error.");
        }
    }

    private Sender parseSender(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("MAIL_FROM must be configured with a valid sender email.");
        }

        int openBracketIndex = trimmed.lastIndexOf('<');
        int closeBracketIndex = trimmed.lastIndexOf('>');
        if (openBracketIndex >= 0 && closeBracketIndex > openBracketIndex) {
            String name = trimmed.substring(0, openBracketIndex).trim();
            String email = trimmed.substring(openBracketIndex + 1, closeBracketIndex).trim();
            validateEmail(email, "MAIL_FROM must contain a valid sender email.");
            return new Sender(name.isEmpty() ? email : name, email);
        }

        validateEmail(trimmed, "MAIL_FROM must contain a valid sender email.");
        return new Sender(trimmed, trimmed);
    }

    private void validateEmail(String email, String message) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException(message);
        }
    }

    private String buildBrevoFailureMessage(int statusCode, String responseBody) {
        if (statusCode == 401 || statusCode == 403) {
            return "Brevo rejected the request. Check BREVO_API_KEY and sender verification in Brevo.";
        }
        if (statusCode == 400) {
            return "Brevo rejected the email payload. Check MAIL_FROM, recipient email, and sender verification in Brevo.";
        }
        if (statusCode == 429) {
            return "Brevo rate limit reached. Wait and retry the email request.";
        }
        if (statusCode >= 500) {
            return "Brevo is temporarily unavailable. Retry in a few moments.";
        }
        return "Brevo email delivery failed with HTTP " + statusCode + ". Response: " + truncate(responseBody);
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitized = value.replaceAll("\\s+", " ").trim();
        if (sanitized.length() <= 220) {
            return sanitized;
        }
        return sanitized.substring(0, 220) + "...";
    }

    private record Sender(String name, String email) {
    }
}
