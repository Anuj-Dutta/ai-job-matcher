package com.anuj.resume_ai_backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final String fromAddress;
    private final String provider;
    private final String gmailClientId;
    private final String gmailClientSecret;
    private final String gmailRefreshToken;
    private final String gmailTokenUrl;
    private final String gmailSendUrl;
    private final boolean mailEnabled;
    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public EmailServiceImpl(
            ObjectMapper objectMapper,
            @Value("${app.mail.from:${MAIL_FROM:}}") String fromAddress,
            @Value("${app.mail.provider:${MAIL_PROVIDER:gmail-api}}") String provider,
            @Value("${app.mail.gmail.client-id:${GMAIL_CLIENT_ID:}}") String gmailClientId,
            @Value("${app.mail.gmail.client-secret:${GMAIL_CLIENT_SECRET:}}") String gmailClientSecret,
            @Value("${app.mail.gmail.refresh-token:${GMAIL_REFRESH_TOKEN:}}") String gmailRefreshToken,
            @Value("${app.mail.gmail.token-url:${GMAIL_TOKEN_URL:https://oauth2.googleapis.com/token}}") String gmailTokenUrl,
            @Value("${app.mail.gmail.send-url:${GMAIL_SEND_URL:https://gmail.googleapis.com/gmail/v1/users/me/messages/send}}") String gmailSendUrl,
            @Value("${app.mail.connection-timeout-ms:${MAIL_CONNECTION_TIMEOUT_MS:5000}}") long connectionTimeoutMs,
            @Value("${app.mail.read-timeout-ms:${MAIL_READ_TIMEOUT_MS:10000}}") long readTimeoutMs,
            @Value("${app.mail.enabled:true}") boolean mailEnabled
    ) {
        this.objectMapper = objectMapper;
        this.fromAddress = normalize(fromAddress);
        this.provider = normalize(provider).toLowerCase();
        this.gmailClientId = normalize(gmailClientId);
        this.gmailClientSecret = normalize(gmailClientSecret);
        this.gmailRefreshToken = normalize(gmailRefreshToken);
        this.gmailTokenUrl = normalize(gmailTokenUrl);
        this.gmailSendUrl = normalize(gmailSendUrl);
        this.mailEnabled = mailEnabled
                && !this.fromAddress.isEmpty()
                && "gmail-api".equals(this.provider)
                && !this.gmailClientId.isEmpty()
                && !this.gmailClientSecret.isEmpty()
                && !this.gmailRefreshToken.isEmpty();
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
            logger.warn("Email delivery skipped for {} because the Gmail API configuration is incomplete.", to);
            return EmailDeliveryResult.skipped("Email delivery is disabled or missing MAIL_FROM/GMAIL_CLIENT_ID/GMAIL_CLIENT_SECRET/GMAIL_REFRESH_TOKEN.");
        }

        try {
            Sender sender = parseSender(fromAddress);
            String accessToken = fetchAccessToken();
            String rawMessage = buildRawMessage(sender, to.trim(), subject, body);
            String payload = objectMapper.writeValueAsString(Map.of("raw", rawMessage));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gmailSendUrl))
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
                logger.info("Email sent successfully to {} via Gmail API", to);
                return EmailDeliveryResult.sent("Email sent successfully.");
            }

            logger.error("Gmail API email delivery failed for {} with status {} and body {}", to, response.statusCode(), truncate(response.body()));
            return EmailDeliveryResult.failed(buildSendFailureMessage(response.statusCode(), response.body()));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize Gmail API email payload for {}", to, e);
            return EmailDeliveryResult.failed("Email payload could not be created. Check the sender address and email content.");
        } catch (IllegalArgumentException e) {
            logger.error("Gmail API email configuration is invalid for {}", to, e);
            return EmailDeliveryResult.failed(e.getMessage());
        } catch (java.net.http.HttpTimeoutException | ConnectException e) {
            logger.error("Gmail API request timed out for {}", to, e);
            return EmailDeliveryResult.failed("Gmail API request timed out. Check outbound HTTPS access from the running environment.");
        } catch (UnknownHostException e) {
            logger.error("Gmail API host could not be resolved for {}", to, e);
            return EmailDeliveryResult.failed("Gmail API host could not be resolved. Check Google API URLs and DNS/network access.");
        } catch (IOException e) {
            logger.error("Gmail API request failed for {}", to, e);
            return EmailDeliveryResult.failed("Gmail API request failed. Check the Google OAuth credentials and outbound HTTPS access.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Gmail API request was interrupted for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery was interrupted before Gmail could respond.");
        } catch (Exception e) {
            logger.error("Unexpected email failure for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed unexpectedly. Check the application logs for the exact error.");
        }
    }

    private String fetchAccessToken() throws IOException, InterruptedException {
        String form = "client_id=" + encode(gmailClientId)
                + "&client_secret=" + encode(gmailClientSecret)
                + "&refresh_token=" + encode(gmailRefreshToken)
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gmailTokenUrl))
                .timeout(requestTimeout)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (!HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
            throw new IllegalArgumentException(buildTokenFailureMessage(response.statusCode(), response.body()));
        }

        JsonNode node = objectMapper.readTree(response.body());
        String accessToken = node.path("access_token").asText("");
        if (accessToken.isBlank()) {
            throw new IllegalArgumentException("Google OAuth token response did not contain an access token.");
        }
        return accessToken;
    }

    private String buildRawMessage(Sender sender, String recipient, String subject, String body) {
        validateEmail(recipient, "Recipient email address is invalid.");
        StringBuilder message = new StringBuilder();
        message.append("From: ").append(formatAddress(sender)).append("\r\n");
        message.append("To: ").append(recipient).append("\r\n");
        message.append("Subject: ").append(sanitizeHeader(subject)).append("\r\n");
        message.append("Content-Type: text/plain; charset=UTF-8\r\n");
        message.append("Content-Transfer-Encoding: 8bit\r\n");
        message.append("\r\n");
        message.append(body == null ? "" : body);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(message.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Sender parseSender(String value) {
        String trimmed = normalize(value);
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

    private String buildTokenFailureMessage(int statusCode, String responseBody) {
        if (statusCode == 400 || statusCode == 401) {
            return "Google OAuth token request failed. Check GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET, and GMAIL_REFRESH_TOKEN.";
        }
        if (statusCode == 429) {
            return "Google OAuth token rate limit reached. Wait and retry the email request.";
        }
        if (statusCode >= 500) {
            return "Google OAuth is temporarily unavailable. Retry in a few moments.";
        }
        return "Google OAuth token request failed with HTTP " + statusCode + ". Response: " + truncate(responseBody);
    }

    private String buildSendFailureMessage(int statusCode, String responseBody) {
        if (statusCode == 400) {
            return "Gmail API rejected the email payload. Check MAIL_FROM, recipient email, and Gmail account permissions.";
        }
        if (statusCode == 401 || statusCode == 403) {
            return "Gmail API rejected the request. Check the OAuth credentials, refresh token, and Gmail API permissions.";
        }
        if (statusCode == 429) {
            return "Gmail API rate limit reached. Wait and retry the email request.";
        }
        if (statusCode >= 500) {
            return "Gmail API is temporarily unavailable. Retry in a few moments.";
        }
        return "Gmail API email delivery failed with HTTP " + statusCode + ". Response: " + truncate(responseBody);
    }

    private String formatAddress(Sender sender) {
        String name = sanitizeHeader(sender.name());
        if (name.equals(sender.email())) {
            return sender.email();
        }
        return name + " <" + sender.email() + ">";
    }

    private String sanitizeHeader(String value) {
        return normalize(value).replace("\r", " ").replace("\n", " ");
    }

    private void validateEmail(String email, String message) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException(message);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record Sender(String name, String email) {
    }
}
