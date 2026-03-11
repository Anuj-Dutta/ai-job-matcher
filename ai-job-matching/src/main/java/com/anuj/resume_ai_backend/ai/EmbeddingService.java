package com.anuj.resume_ai_backend.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmbeddingService {

    static final int FALLBACK_DIMENSIONS = 384;
    private static final int MAX_INPUT_LENGTH = 1000;

    private final RestTemplate restTemplate;
    private final String hfApi;
    private final String hfToken;

    @Autowired
    public EmbeddingService(
            @Value("${ai.embedding.hf.api:https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/all-MiniLM-L6-v2}") String hfApi,
            @Value("${HF_TOKEN:}") String hfToken
    ) {
        this(new RestTemplate(), hfApi, hfToken);
    }

    EmbeddingService(RestTemplate restTemplate, String hfApi, String hfToken) {
        this.restTemplate = restTemplate;
        this.hfApi = hfApi;
        this.hfToken = hfToken == null ? "" : hfToken.trim();
    }

    public String generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            System.out.println("Empty text received for embedding.");
            return null;
        }

        String sanitizedText = text.strip();
        if (sanitizedText.length() > MAX_INPUT_LENGTH) {
            sanitizedText = sanitizedText.substring(0, MAX_INPUT_LENGTH);
        }

        try {
            if (!hfToken.isEmpty()) {
                String remoteEmbedding = generateRemoteEmbedding(sanitizedText);
                if (remoteEmbedding != null) {
                    return remoteEmbedding;
                }
            } else {
                System.out.println("HF_TOKEN not configured. Using local fallback embedding.");
            }
        } catch (Exception e) {
            System.out.println("Embedding API error. Falling back to local embedding.");
            e.printStackTrace();
        }

        return generateLocalEmbedding(sanitizedText);
    }

    private String generateRemoteEmbedding(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(hfToken);

        Map<String, String> body = new HashMap<>();
        body.put("inputs", text);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<List> response = restTemplate.exchange(hfApi, HttpMethod.POST, request, List.class);

        List<Double> embedding = parseEmbeddingResponse(response.getBody());
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }

        return toCsv(embedding);
    }

    private List<Double> parseEmbeddingResponse(List<?> responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            System.out.println("Embedding response empty.");
            return null;
        }

        Object firstItem = responseBody.get(0);

        if (firstItem instanceof Number) {
            List<Double> embedding = new ArrayList<>(responseBody.size());
            for (Object item : responseBody) {
                if (!(item instanceof Number number)) {
                    return null;
                }
                embedding.add(number.doubleValue());
            }
            return embedding;
        }

        if (firstItem instanceof List<?>) {
            return meanPool(responseBody);
        }

        System.out.println("Unexpected embedding format: " + firstItem.getClass().getName());
        return null;
    }

    private List<Double> meanPool(List<?> tokenEmbeddings) {
        List<Double> pooled = new ArrayList<>();
        int tokenCount = 0;

        for (Object tokenEmbedding : tokenEmbeddings) {
            if (!(tokenEmbedding instanceof List<?> tokenValues) || tokenValues.isEmpty()) {
                continue;
            }

            if (pooled.isEmpty()) {
                for (Object value : tokenValues) {
                    if (!(value instanceof Number number)) {
                        return null;
                    }
                    pooled.add(number.doubleValue());
                }
            } else {
                if (tokenValues.size() != pooled.size()) {
                    return null;
                }
                for (int i = 0; i < tokenValues.size(); i++) {
                    Object value = tokenValues.get(i);
                    if (!(value instanceof Number number)) {
                        return null;
                    }
                    pooled.set(i, pooled.get(i) + number.doubleValue());
                }
            }

            tokenCount++;
        }

        if (tokenCount == 0) {
            return null;
        }

        for (int i = 0; i < pooled.size(); i++) {
            pooled.set(i, pooled.get(i) / tokenCount);
        }

        return pooled;
    }

    private String generateLocalEmbedding(String text) {
        double[] vector = new double[FALLBACK_DIMENSIONS];
        String[] tokens = text
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim()
                .split("\\s+");

        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }

            int bucket = Math.floorMod(token.hashCode(), FALLBACK_DIMENSIONS);
            vector[bucket] += 1.0;
        }

        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }

        if (norm == 0.0) {
            vector[0] = 1.0;
        } else {
            double magnitude = Math.sqrt(norm);
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / magnitude;
            }
        }

        return toCsv(vector);
    }

    private String toCsv(List<Double> values) {
        StringBuilder vector = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            vector.append(values.get(i));
            if (i < values.size() - 1) {
                vector.append(',');
            }
        }
        return vector.toString();
    }

    private String toCsv(double[] values) {
        StringBuilder vector = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            vector.append(values[i]);
            if (i < values.length - 1) {
                vector.append(',');
            }
        }
        return vector.toString();
    }
}


