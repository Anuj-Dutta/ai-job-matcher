package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SimilarityService;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchingServiceImpl implements MatchingService {

    private final JobRepository jobRepository;
    private final SimilarityService similarityService;
    private final EmbeddingService embeddingService;

   public MatchingServiceImpl(JobRepository jobRepository,SimilarityService similarityService,EmbeddingService embeddingService) {
    this.jobRepository = jobRepository;
    this.similarityService = similarityService;
    this.embeddingService = embeddingService;
    }

    @Override
    public List<Job> matchJobs(Resume resume) {

    String skills = resume.getSkillsJson();

    if (skills == null || skills.isEmpty()) {
        return new ArrayList<>();
    }

String firstSkill = skills
        .replace("[", "")
        .replace("]", "")
        .split(",")[0]
        .trim();

List<Job> jobs = jobRepository.findAll();
    List<Job> matched = new ArrayList<>();
    Set<String> missingSkills = new HashSet<>();

    if (resume.getEmbedding() == null) {
        return matched;
    }

    for (Job job : jobs) {

    if (job.getEmbedding() == null) {
        String embedding = embeddingService.generateEmbedding(job.getDescription());
        job.setEmbedding(embedding);
        jobRepository.save(job);
    }

    double score = similarityService.cosineSimilarity(
            resume.getEmbedding(),
            job.getEmbedding()
    );
System.out.println("Similarity score: " + score);

        if (score > 0.35) {

    job.setMatchScore(score);
    matched.add(job);

    if (job.getSkills() != null && resume.getSkillsJson() != null) {

        String resumeSkills = resume.getSkillsJson().toLowerCase();
        String[] jobSkills = job.getSkills().toLowerCase().split(",");

        for (String skill : jobSkills) {

            String trimmed = skill.trim();

            if (!resumeSkills.contains(trimmed)) {
                missingSkills.add(trimmed);
            }

        }
    }
}
    }

    matched.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

    System.out.println("Missing skills suggestion: " + missingSkills);
    return matched.stream().limit(10).toList();
    }
}