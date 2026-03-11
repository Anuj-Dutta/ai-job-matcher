package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.repository.JobRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdzunaServiceTest {

    @Test
    void skipsImportWhenCredentialsAreMissing() {
        JobRepository jobRepository = mock(JobRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        AdzunaService adzunaService = new AdzunaService(jobRepository, embeddingService, "", "", "in");

        String result = adzunaService.importJobs();

        assertTrue(result.contains("not configured"));
    }

    @Test
    void backfillsOnlyJobsMissingEmbeddings() {
        JobRepository jobRepository = mock(JobRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        JobServiceImpl jobService = new JobServiceImpl(jobRepository, embeddingService);

        Job missingEmbedding = new Job();
        missingEmbedding.setTitle("Backend Engineer");
        missingEmbedding.setDescription("Java Spring Boot PostgreSQL");

        Job existingEmbedding = new Job();
        existingEmbedding.setTitle("Data Engineer");
        existingEmbedding.setEmbedding("0.1,0.2,0.3");

        when(jobRepository.findAll()).thenReturn(List.of(missingEmbedding, existingEmbedding));
        when(embeddingService.generateEmbedding("Backend Engineer Java Spring Boot PostgreSQL"))
                .thenReturn("0.4,0.5,0.6");

        int updated = jobService.backfillMissingEmbeddings();

        assertEquals(1, updated);
        assertEquals("0.4,0.5,0.6", missingEmbedding.getEmbedding());
        verify(jobRepository, times(1)).save(missingEmbedding);
    }
}
