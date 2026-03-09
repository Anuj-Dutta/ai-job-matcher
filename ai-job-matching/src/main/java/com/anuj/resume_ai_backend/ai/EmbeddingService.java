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

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(HF_TOKEN);

            Map<String, String> body = new HashMap<>();
            body.put("inputs", text);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<List> response =
                    restTemplate.exchange(HF_API, HttpMethod.POST, request, List.class);

            List<?> outer = response.getBody();

            if (outer == null || outer.isEmpty()) {
                System.out.println("Embedding response empty");
                return null;
            }

            List<Double> embedding;

            if (outer.get(0) instanceof List) {
                embedding = (List<Double>) outer.get(0);
            } else {
                System.out.println("Unexpected embedding format");
                return null;
            }

            StringBuilder vector = new StringBuilder();

            for (int i = 0; i < embedding.size(); i++) {
                vector.append(embedding.get(i));
                if (i < embedding.size() - 1) {
                    vector.append(",");
                }
            }

            return vector.toString();

        } catch (Exception e) {

            System.out.println("Embedding API error: " + e.getMessage());
            return null;

        }
    }
}