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

            if (text == null || text.isEmpty()) {
                System.out.println("Empty text received for embedding.");
                return null;
            }

            // Trim huge descriptions (HF sometimes rejects long input)
            if (text.length() > 1000) {
                text = text.substring(0, 1000);
            }

            System.out.println("Generating embedding for text length: " + text.length());
            System.out.println("HF TOKEN PRESENT: " + (HF_TOKEN != null));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(HF_TOKEN);

            Map<String, String> body = new HashMap<>();
            body.put("inputs", text);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<List> response =
                    restTemplate.exchange(HF_API, HttpMethod.POST, request, List.class);

            System.out.println("HF RESPONSE BODY: " + response.getBody());

            List<?> outer = response.getBody();

            if (outer == null || outer.isEmpty()) {
                System.out.println("Embedding response empty.");
                return null;
            }

            List<Double> embedding;

            if (outer.get(0) instanceof List) {
                embedding = (List<Double>) outer.get(0);
            } else {
                System.out.println("Unexpected embedding format.");
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

            System.out.println("Embedding API error:");
            e.printStackTrace();
            return null;
        }
    }
}