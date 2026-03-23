package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SkillExtractor;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final EmbeddingService embeddingService;

    public JobServiceImpl(JobRepository jobRepository, EmbeddingService embeddingService) {
        this.jobRepository = jobRepository;
        this.embeddingService = embeddingService;
    }

    @Override
    public Job saveJob(Job job) {
        hydrateDerivedFields(job);
        job.setEmbedding(embeddingService.generateEmbedding(buildJobText(job)));
        return jobRepository.save(job);
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public int backfillMissingEmbeddings() {
        int updated = 0;

        for (Job job : jobRepository.findAll()) {
            if (job.getEmbedding() != null && !job.getEmbedding().isBlank()) {
                continue;
            }

            hydrateDerivedFields(job);
            String embedding = embeddingService.generateEmbedding(buildJobText(job));
            if (embedding == null || embedding.isBlank()) {
                continue;
            }

            job.setEmbedding(embedding);
            jobRepository.save(job);
            updated++;
        }

        return updated;
    }

    private void hydrateDerivedFields(Job job) {
        if (job == null) {
            return;
        }

        if (job.getSkills() == null || job.getSkills().isBlank()) {
            job.setSkills(SkillExtractor.extractSkillsAsCsv(buildJobText(job)));
        }
    }

    private String buildJobText(Job job) {
        StringBuilder builder = new StringBuilder();
        appendIfPresent(builder, job.getTitle());
        appendIfPresent(builder, job.getCompany());
        appendIfPresent(builder, job.getLocation());
        appendIfPresent(builder, job.getDescription());
        appendIfPresent(builder, job.getSkills());
        return builder.toString().trim();
    }

    private void appendIfPresent(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }
}
