package com.anuj.resume_ai_backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String HF_API =
            "https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/all-MiniLM-L6-v2";

    private final String HF_TOKEN = System.getenv("HF_TOKEN");

    public String generateEmbedding(String text) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(HF_TOKEN);

        HttpEntity<String> request =
                new HttpEntity<>("{\"inputs\":\"" + text + "\"}", headers);

        ResponseEntity<List> response =
                restTemplate.exchange(HF_API, HttpMethod.POST, request, List.class);

        List<Double> embedding = (List<Double>) response.getBody().get(0);

        StringBuilder vector = new StringBuilder();

        for (int i = 0; i < embedding.size(); i++) {
            vector.append(embedding.get(i));
            if (i < embedding.size() - 1) vector.append(",");
        }

        return vector.toString();
    }
}