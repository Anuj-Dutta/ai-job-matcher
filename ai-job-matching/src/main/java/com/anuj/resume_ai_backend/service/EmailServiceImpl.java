package com.anuj.resume_ai_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String DEFAULT_FROM_ADDRESS = "AI Job Matcher <onboarding@resend.dev>";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String fromAddress;
    private final boolean mailEnabled;

    public EmailServiceImpl(
            ObjectMapper objectMapper,
            @Value("${app.mail.api-key:${RESEND_API_KEY:}}") String apiKey,
            @Value("${app.mail.from:${MAIL_FROM:}}") String fromAddress,
            @Value("${app.mail.enabled:true}") boolean mailEnabled
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.apiKey = apiKey == null ? "" : apiKey.trim();

        String normalizedFromAddress = fromAddress == null ? "" : fromAddress.trim();
        this.fromAddress = normalizedFromAddress.isEmpty() ? DEFAULT_FROM_ADDRESS : normalizedFromAddress;
        this.mailEnabled = mailEnabled && !this.apiKey.isEmpty();
    }

    @Override
    public EmailDeliveryResult sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            logger.warn("Email delivery skipped because no recipient address was provided.");
            return EmailDeliveryResult.skipped("No recipient email address was provided.");
        }

        if (!mailEnabled) {
            logger.warn("Email delivery skipped for {} because the Resend configuration is incomplete.", to);
            return EmailDeliveryResult.skipped("Email delivery is disabled or missing RESEND_API_KEY.");
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "from", fromAddress,
                    "to", List.of(to),
                    "subject", subject,
                    "text", body
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Email sent successfully to {} via Resend", to);
                return EmailDeliveryResult.sent("Email sent successfully.");
            }

            String errorMessage = extractErrorMessage(response.body());
            logger.error("Resend rejected email for {} with status {} and body {}", to, response.statusCode(), response.body());
            return EmailDeliveryResult.failed("Email delivery failed: " + errorMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Email delivery interrupted for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery was interrupted. Please try again.");
        } catch (IOException e) {
            logger.error("Network error while sending email to {} via Resend", to, e);
            return EmailDeliveryResult.failed("Email delivery failed due to a network error while contacting Resend.");
        } catch (Exception e) {
            logger.error("Unexpected email failure for {}", to, e);
            return EmailDeliveryResult.failed("Email delivery failed unexpectedly. Check the Render logs for the exact error.");
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "The email API returned an empty error response.";
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.hasNonNull("message")) {
                return jsonNode.get("message").asText();
            }

            if (jsonNode.hasNonNull("error")) {
                return jsonNode.get("error").asText();
            }
        } catch (Exception ignored) {
            // Fall back to the raw response body when the API response is not JSON.
        }

        return responseBody;
    }
}
