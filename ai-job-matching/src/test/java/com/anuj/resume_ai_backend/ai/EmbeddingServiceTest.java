package com.anuj.resume_ai_backend.ai;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmbeddingServiceTest {

    @Test
    void fallsBackToLocalEmbeddingWhenTokenIsMissing() {
        EmbeddingService service = new EmbeddingService(new RestTemplate(), "https://example.invalid", "");

        String embedding = service.generateEmbedding("Java Spring Boot PostgreSQL");

        assertNotNull(embedding);
        assertEquals(EmbeddingService.FALLBACK_DIMENSIONS, embedding.split(",").length);
    }

    @Test
    void returnsNullForBlankText() {
        EmbeddingService service = new EmbeddingService(new RestTemplate(), "https://example.invalid", "");

        String embedding = service.generateEmbedding("   ");

        assertEquals(null, embedding);
    }
}
