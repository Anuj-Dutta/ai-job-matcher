package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.repository.JobRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AdzunaServiceTest {

    @Test
    void skipsImportWhenCredentialsAreMissing() {
        JobRepository jobRepository = mock(JobRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        AdzunaService adzunaService = new AdzunaService(jobRepository, embeddingService, "", "", "in");

        String result = adzunaService.importJobs();

        assertTrue(result.contains("not configured"));
    }
}
