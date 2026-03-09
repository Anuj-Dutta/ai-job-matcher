package com.anuj.resume_ai_backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${HF_TOKEN}")
    private String hfToken;

    public String generateEmbedding(String text) {

        String url = "https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/all-MiniLM-L6-v2";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(hfToken);

        Map<String, String> request = new HashMap<>();
        request.put("inputs", text);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

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