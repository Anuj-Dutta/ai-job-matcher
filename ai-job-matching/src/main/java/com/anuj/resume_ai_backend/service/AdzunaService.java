package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SkillExtractor;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class AdzunaService {

    private static final int MAX_IMPORT = 400;
    private static final int MAX_DB_SIZE = 12000;
    private static final int RESULTS_PER_PAGE = 25;
    private static final List<String> SEARCH_QUERIES = List.of(
            "software engineer", "java developer", "python developer", "frontend developer", "backend developer",
            "full stack developer", "mobile app developer", "devops engineer", "cloud engineer", "site reliability engineer",
            "data analyst", "data engineer", "data scientist", "business analyst", "product manager",
            "project manager", "scrum master", "qa engineer", "automation tester", "cyber security analyst",
            "network engineer", "system administrator", "technical support", "customer support", "customer success",
            "sales executive", "business development", "digital marketing", "seo specialist", "content writer",
            "graphic designer", "ui ux designer", "operations executive", "supply chain analyst", "procurement specialist",
            "accountant", "financial analyst", "hr executive", "recruiter", "talent acquisition",
            "teacher", "education counselor", "healthcare assistant", "nurse", "pharmacist"
    );

    private final JobRepository jobRepository;
    private final EmbeddingService embeddingService;
    private final RestTemplate restTemplate;
    private final String adzunaAppId;
    private final String adzunaAppKey;
    private final String adzunaCountry;

    public AdzunaService(
            JobRepository jobRepository,
            EmbeddingService embeddingService,
            @Value("${adzuna.app_id:}") String adzunaAppId,
            @Value("${adzuna.app_key:}") String adzunaAppKey,
            @Value("${adzuna.country:in}") String adzunaCountry
    ) {
        this.jobRepository = jobRepository;
        this.embeddingService = embeddingService;
        this.restTemplate = new RestTemplate();
        this.adzunaAppId = adzunaAppId == null ? "" : adzunaAppId.trim();
        this.adzunaAppKey = adzunaAppKey == null ? "" : adzunaAppKey.trim();
        this.adzunaCountry = (adzunaCountry == null || adzunaCountry.isBlank()) ? "in" : adzunaCountry.trim();
    }

    public String importJobs() {
        if (adzunaAppId.isEmpty() || adzunaAppKey.isEmpty()) {
            String message = "Adzuna credentials are not configured. Skipping import.";
            System.out.println(message);
            return message;
        }

        int imported = 0;

        if (jobRepository.count() > MAX_DB_SIZE) {
            System.out.println("Job database large enough. Skipping import.");
            return "Import skipped";
        }

        for (String query : SEARCH_QUERIES) {
            for (int page = 1; page <= 2; page++) {
                if (imported >= MAX_IMPORT) {
                    return "Imported " + imported + " jobs";
                }

                String url = UriComponentsBuilder
                        .fromHttpUrl("https://api.adzuna.com/v1/api/jobs/{country}/search/{page}")
                        .queryParam("app_id", adzunaAppId)
                        .queryParam("app_key", adzunaAppKey)
                        .queryParam("what", query)
                        .queryParam("results_per_page", RESULTS_PER_PAGE)
                        .queryParam("content-type", "application/json")
                        .buildAndExpand(adzunaCountry, page)
                        .toUriString();

                System.out.println("Fetching Adzuna jobs for query '" + query + "' page " + page);

                Map response;
                try {
                    response = restTemplate.getForObject(url, Map.class);
                } catch (Exception e) {
                    System.out.println("Adzuna API error for query '" + query + "' page " + page);
                    e.printStackTrace();
                    continue;
                }

                if (response == null) {
                    continue;
                }

                Object resultsObj = response.get("results");
                if (!(resultsObj instanceof List<?> rawResults) || rawResults.isEmpty()) {
                    continue;
                }

                for (Object item : rawResults) {
                    if (!(item instanceof Map<?, ?> rawJobData)) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> jobData = (Map<String, Object>) rawJobData;

                    if (imported >= MAX_IMPORT) {
                        return "Imported " + imported + " jobs";
                    }

                    String externalId = String.valueOf(jobData.get("adref"));
                    if (externalId.isBlank() || jobRepository.existsByExternalId(externalId)) {
                        continue;
                    }

                    Job job = new Job();
                    job.setExternalId(externalId);
                    job.setTitle(asString(jobData.get("title")));
                    job.setDescription(asString(jobData.get("description")));
                    job.setApplyLink(asString(jobData.get("redirect_url")));
                    job.setCompany(extractNestedDisplayName(jobData.get("company")));
                    job.setLocation(extractNestedDisplayName(jobData.get("location")));
                    job.setSkills(SkillExtractor.extractSkillsAsCsv(buildJobText(job)));
                    job.setEmbedding(embeddingService.generateEmbedding(buildJobText(job)));

                    jobRepository.save(job);
                    imported++;
                }
            }
        }

        System.out.println("Imported jobs: " + imported);
        return "Imported " + imported + " jobs";
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

    private String extractNestedDisplayName(Object nestedObject) {
        if (!(nestedObject instanceof Map<?, ?> nestedMap)) {
            return null;
        }

        Object displayName = nestedMap.get("display_name");
        return displayName == null ? null : displayName.toString();
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
