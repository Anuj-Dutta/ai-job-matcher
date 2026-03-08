package com.anuj.resume_ai_backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;
import java.util.List;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateEmbedding(String text) {

        String url = "http://localhost:11434/api/embeddings";

        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "prompt", text
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        Map response = restTemplate.postForObject(url, entity, Map.class);

        List<Double> embedding = (List<Double>) response.get("embedding");

        StringBuilder vector = new StringBuilder();

        for (int i = 0; i < embedding.size(); i++) {
            vector.append(embedding.get(i));
            if (i < embedding.size() - 1) {
                vector.append(",");
            }
        }

        return vector.toString();
    }
}