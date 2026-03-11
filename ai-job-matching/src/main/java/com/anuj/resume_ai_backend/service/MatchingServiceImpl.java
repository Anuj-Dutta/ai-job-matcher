package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SimilarityService;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.JobRepository;
import com.anuj.resume_ai_backend.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchingServiceImpl implements MatchingService {

    private static final double MATCH_THRESHOLD = 0.35;

    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final SimilarityService similarityService;
    private final EmbeddingService embeddingService;

    public MatchingServiceImpl(
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            SimilarityService similarityService,
            EmbeddingService embeddingService
    ) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.similarityService = similarityService;
        this.embeddingService = embeddingService;
    }

    @Override
    public List<Job> matchJobs(Resume resume) {

        String skills = resume.getSkillsJson();
        if (skills == null || skills.isBlank()) {
            return new ArrayList<>();
        }

        ensureResumeEmbedding(resume);

        if (resume.getEmbedding() == null || resume.getEmbedding().isBlank()) {
            return new ArrayList<>();
        }

        List<Job> jobs = jobRepository.findAll();
        List<Job> matched = new ArrayList<>();
        Set<String> missingSkills = new HashSet<>();

        for (Job job : jobs) {

            ensureJobEmbedding(job);

            if (job.getEmbedding() == null || job.getEmbedding().isBlank()) {
                continue;
            }

            double score = similarityService.cosineSimilarity(
                    resume.getEmbedding(),
                    job.getEmbedding()
            );

            System.out.println("Similarity score: " + score);

            if (score > MATCH_THRESHOLD) {

                job.setMatchScore(score);
                matched.add(job);

                collectMissingSkills(resume, job, missingSkills);
            }
        }

        // sort by similarity score
        matched.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        // remove duplicate jobs (same title + company + location)
        Set<String> seen = new HashSet<>();
        List<Job> uniqueJobs = new ArrayList<>();

        for (Job job : matched) {

            String key =
                    job.getTitle() +
                    job.getCompany() +
                    job.getLocation();

            if (!seen.contains(key)) {
                seen.add(key);
                uniqueJobs.add(job);
            }
        }

        System.out.println("Missing skills suggestion: " + missingSkills);

        // return top 10 unique matches
        return uniqueJobs.stream().limit(10).toList();
    }

    private void ensureResumeEmbedding(Resume resume) {

        if (resume.getEmbedding() != null && !resume.getEmbedding().isBlank()) {
            return;
        }

        String embedding =
                embeddingService.generateEmbedding(resume.getResumeText());

        if (embedding != null && !embedding.isBlank()) {

            resume.setEmbedding(embedding);
            resumeRepository.save(resume);
        }
    }

    private void ensureJobEmbedding(Job job) {

        if (job.getEmbedding() != null && !job.getEmbedding().isBlank()) {
            return;
        }

        String embedding =
                embeddingService.generateEmbedding(buildJobText(job));

        if (embedding != null && !embedding.isBlank()) {

            job.setEmbedding(embedding);
            jobRepository.save(job);
        }
    }

    private void collectMissingSkills(
            Resume resume,
            Job job,
            Set<String> missingSkills
    ) {

        if (job.getSkills() == null || resume.getSkillsJson() == null) {
            return;
        }

        String resumeSkills = resume.getSkillsJson().toLowerCase();
        String[] jobSkills = job.getSkills().toLowerCase().split(",");

        for (String skill : jobSkills) {

            String trimmed = skill.trim();

            if (!trimmed.isEmpty() && !resumeSkills.contains(trimmed)) {
                missingSkills.add(trimmed);
            }
        }
    }

    private String buildJobText(Job job) {

        StringBuilder builder = new StringBuilder();

        appendIfPresent(builder, job.getTitle());
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