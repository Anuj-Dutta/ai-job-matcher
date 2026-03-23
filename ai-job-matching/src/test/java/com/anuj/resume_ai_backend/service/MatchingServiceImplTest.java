package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SimilarityService;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.JobRepository;
import com.anuj.resume_ai_backend.repository.ResumeRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchingServiceImplTest {

    @Test
    void ranksDomainRelevantJobsHigherThanGenericEmbeddingOnlyMatches() {
        JobRepository jobRepository = mock(JobRepository.class);
        ResumeRepository resumeRepository = mock(ResumeRepository.class);
        SimilarityService similarityService = mock(SimilarityService.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);

        MatchingServiceImpl service = new MatchingServiceImpl(
                jobRepository,
                resumeRepository,
                similarityService,
                embeddingService
        );

        Resume resume = new Resume();
        resume.setResumeText("Accountant with finance reporting, bookkeeping, accounting and Excel experience");
        resume.setSkillsJson("");
        resume.setEmbedding("0.5,0.5");

        Job accountantJob = new Job();
        accountantJob.setTitle("Accountant");
        accountantJob.setCompany("Acme Finance");
        accountantJob.setLocation("Mumbai");
        accountantJob.setDescription("Accounting, bookkeeping, finance reporting, Excel and reconciliations");
        accountantJob.setSkills("accounting, bookkeeping, finance, excel");
        accountantJob.setEmbedding("0.5,0.5");

        Job developerJob = new Job();
        developerJob.setTitle("Software Engineer");
        developerJob.setCompany("Tech Labs");
        developerJob.setLocation("Pune");
        developerJob.setDescription("Java Spring Boot microservices and distributed systems");
        developerJob.setSkills("java, spring boot, microservices");
        developerJob.setEmbedding("0.5,0.5");

        when(jobRepository.findAll()).thenReturn(List.of(accountantJob, developerJob));
        when(similarityService.cosineSimilarity(anyString(), anyString())).thenReturn(0.40);

        List<Job> matches = service.matchJobs(resume);

        assertFalse(matches.isEmpty());
        assertEquals("Accountant", matches.get(0).getTitle());
        verify(resumeRepository).save(resume);
        verify(embeddingService, never()).generateEmbedding(resume.getResumeText());
    }

    @Test
    void returnsFallbackMatchesForNonDeveloperResumeWhenNoJobClearsPrimaryThreshold() {
        JobRepository jobRepository = mock(JobRepository.class);
        ResumeRepository resumeRepository = mock(ResumeRepository.class);
        SimilarityService similarityService = mock(SimilarityService.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);

        MatchingServiceImpl service = new MatchingServiceImpl(
                jobRepository,
                resumeRepository,
                similarityService,
                embeddingService
        );

        Resume resume = new Resume();
        resume.setResumeText("Customer support specialist with customer success, ticket handling and CRM experience");
        resume.setSkillsJson("customer support, customer success, crm");
        resume.setEmbedding("0.1,0.1");

        Job supportJob = new Job();
        supportJob.setTitle("Customer Success Associate");
        supportJob.setCompany("Service Desk");
        supportJob.setLocation("Delhi");
        supportJob.setDescription("Customer support, CRM, onboarding and issue resolution");
        supportJob.setSkills("customer support, customer success, crm");
        supportJob.setEmbedding("0.1,0.2");

        when(jobRepository.findAll()).thenReturn(List.of(supportJob));
        when(similarityService.cosineSimilarity(anyString(), anyString())).thenReturn(0.05);

        List<Job> matches = service.matchJobs(resume);

        assertEquals(1, matches.size());
        assertEquals("Customer Success Associate", matches.get(0).getTitle());
        assertFalse(matches.get(0).getMatchScore() < 0.08);
    }
}
