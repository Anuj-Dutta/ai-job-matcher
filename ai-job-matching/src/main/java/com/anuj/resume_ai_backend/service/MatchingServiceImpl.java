package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SimilarityService;
import com.anuj.resume_ai_backend.ai.SkillExtractor;
import com.anuj.resume_ai_backend.ai.TextProfileUtils;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.JobRepository;
import com.anuj.resume_ai_backend.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MatchingServiceImpl implements MatchingService {

    private static final double MATCH_THRESHOLD = 0.23;
    private static final double MINIMUM_FALLBACK_THRESHOLD = 0.08;

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
        ensureResumeProfile(resume);
        ensureResumeEmbedding(resume);

        String resumeText = safeLower(resume.getResumeText());
        if (resumeText.isBlank()) {
            return new ArrayList<>();
        }

        Set<String> resumeSkills = TextProfileUtils.parseSkills(resume.getSkillsJson());
        Set<String> resumeTokens = TextProfileUtils.tokenize(resumeText);

        List<Job> jobs = jobRepository.findAll();
        List<Job> matched = new ArrayList<>();
        Set<String> missingSkills = new HashSet<>();

        for (Job job : jobs) {
            ensureJobProfile(job);
            ensureJobEmbedding(job);

            String jobText = buildJobText(job);
            if (jobText.isBlank()) {
                continue;
            }

            double score = calculateHybridScore(resume, resumeSkills, resumeTokens, job, jobText);

            System.out.println("Similarity score: " + score);

            if (score > MATCH_THRESHOLD) {
                job.setMatchScore(score);
                matched.add(job);

                collectMissingSkills(resumeSkills, job, missingSkills);
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

            addIfUnique(job, seen, uniqueJobs);
        }

        if (matched.isEmpty()) {
            for (Job job : jobs) {
                String jobText = buildJobText(job);
                if (jobText.isBlank()) {
                    continue;
                }

                double score = calculateHybridScore(resume, resumeSkills, resumeTokens, job, jobText);
                if (score >= MINIMUM_FALLBACK_THRESHOLD) {
                    job.setMatchScore(score);
                    addIfUnique(job, seen, uniqueJobs);
                }
            }
            uniqueJobs.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        }

        System.out.println("Missing skills suggestion: " + missingSkills);

        // return top 10 unique matches
        return uniqueJobs.stream().limit(10).toList();
    }

    private void ensureResumeProfile(Resume resume) {
        if (resume.getSkillsJson() != null && !resume.getSkillsJson().isBlank()) {
            return;
        }

        String extractedSkills = SkillExtractor.extractSkillsAsCsv(resume.getResumeText());
        if (!extractedSkills.isBlank()) {
            resume.setSkillsJson(extractedSkills);
            resumeRepository.save(resume);
        }
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

    private void ensureJobProfile(Job job) {
        if (job.getSkills() != null && !job.getSkills().isBlank()) {
            return;
        }

        String extractedSkills = SkillExtractor.extractSkillsAsCsv(buildJobText(job));
        if (!extractedSkills.isBlank()) {
            job.setSkills(extractedSkills);
            jobRepository.save(job);
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

    private double calculateHybridScore(
            Resume resume,
            Set<String> resumeSkills,
            Set<String> resumeTokens,
            Job job,
            String jobText
    ) {
        double embeddingScore = 0.0;
        if (resume.getEmbedding() != null && !resume.getEmbedding().isBlank()
                && job.getEmbedding() != null && !job.getEmbedding().isBlank()) {
            embeddingScore = Math.max(0.0, similarityService.cosineSimilarity(resume.getEmbedding(), job.getEmbedding()));
        }

        Set<String> jobSkills = TextProfileUtils.parseSkills(job.getSkills());
        Set<String> jobTokens = TextProfileUtils.tokenize(jobText);
        Set<String> titleTokens = TextProfileUtils.tokenize(job.getTitle());

        double skillScore = TextProfileUtils.overlapScore(resumeSkills, jobSkills);
        double keywordScore = TextProfileUtils.overlapScore(resumeTokens, jobTokens);
        double titleScore = TextProfileUtils.overlapScore(resumeTokens, titleTokens);

        double exactBoost = hasStrongPhraseOverlap(resume, job) ? 0.08 : 0.0;
        return (0.50 * embeddingScore)
                + (0.25 * skillScore)
                + (0.15 * keywordScore)
                + (0.10 * titleScore)
                + exactBoost;
    }

    private boolean hasStrongPhraseOverlap(Resume resume, Job job) {
        String resumeText = safeLower(resume.getResumeText());
        String title = safeLower(job.getTitle());
        if (resumeText.isBlank() || title.isBlank()) {
            return false;
        }

        for (String phrase : List.of(title, safeLower(job.getCompany()), safeLower(job.getLocation()))) {
            if (!phrase.isBlank() && phrase.length() >= 5 && resumeText.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    private void collectMissingSkills(
            Set<String> resumeSkills,
            Job job,
            Set<String> missingSkills
    ) {
        if (resumeSkills == null || resumeSkills.isEmpty() || job.getSkills() == null) {
            return;
        }

        Set<String> jobSkills = TextProfileUtils.parseSkills(job.getSkills());

        for (String skill : jobSkills) {
            if (!skill.isEmpty() && !resumeSkills.contains(skill)) {
                missingSkills.add(skill);
            }
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

    private void addIfUnique(Job job, Set<String> seen, List<Job> uniqueJobs) {
        String key = safeLower(job.getTitle()) + "|" + safeLower(job.getCompany()) + "|" + safeLower(job.getLocation());
        if (seen.add(key)) {
            uniqueJobs.add(job);
        }
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
