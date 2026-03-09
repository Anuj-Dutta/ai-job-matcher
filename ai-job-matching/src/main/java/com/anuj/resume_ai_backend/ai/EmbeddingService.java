package com.anuj.resume_ai_backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateEmbedding(String text) {

        String url = "https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/all-MiniLM-L6-v2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String token = System.getenv("ai-job-matcher");
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(text, headers);

        List<List<Double>> response =
                restTemplate.postForObject(url, entity, List.class);

        List<Double> embedding = response.get(0);

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