package com.anuj.resume_ai_backend.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimilarityServiceTest {

    private final SimilarityService similarityService = new SimilarityService();

    @Test
    void returnsZeroForInvalidVectorValues() {
        assertEquals(0.0, similarityService.cosineSimilarity("1.0,abc", "1.0,2.0"));
    }

    @Test
    void returnsZeroForZeroNormVectors() {
        assertEquals(0.0, similarityService.cosineSimilarity("0,0,0", "0,0,0"));
    }

    @Test
    void calculatesCosineSimilarityForValidVectors() {
        assertEquals(1.0, similarityService.cosineSimilarity("1,0,0", "1,0,0"));
    }
}
