package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AdzunaService {

    private final JobRepository jobRepository;
    private final EmbeddingService embeddingService;

    public AdzunaService(JobRepository jobRepository, EmbeddingService embeddingService) {
        this.jobRepository = jobRepository;
        this.embeddingService = embeddingService;
    }

    public String importJobs() {

    if (jobRepository.count() > 3000) {
        System.out.println("Job database large enough. Skipping import.");
        return "Import skipped";
    }

    String[] queries = {
            "developer",
            "software engineer",
            "java developer",
            "python developer",
            "backend engineer",
            "data engineer",
            "cloud engineer",
            "programmer"
    };

    int imported = 0;

    RestTemplate restTemplate = new RestTemplate();

    for (String query : queries) {

        for (int page = 1; page <= 3; page++) {

            String url =
                    "https://api.adzuna.com/v1/api/jobs/in/search/" + page +
                    "?app_id=36c883a0" +
                    "&app_key=e7f7fe6902e449b4b2f979bdd4f27fae" +
                    "&what=" + query.replace(" ", "%20") +
                    "&results_per_page=50";

            System.out.println("Fetching: " + query + " page " + page);

            Map response;

            try {
                response = restTemplate.getForObject(url, Map.class);
            } catch (Exception e) {
                System.out.println("API error, skipping...");
                continue;
            }

            Object resultsObj = response.get("results");

            if (!(resultsObj instanceof List)) {
                continue;
            }

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) resultsObj;

            if (results.isEmpty()) {
                continue;
            }

            for (Map<String, Object> jobData : results) {

                String externalId = String.valueOf(jobData.get("adref"));

                if (jobRepository.existsByExternalId(externalId)) {
                    continue;
                }

                Job job = new Job();

                job.setExternalId(externalId);
                job.setTitle((String) jobData.get("title"));
                job.setDescription((String) jobData.get("description"));

                Map company = (Map) jobData.get("company");
                if (company != null)
                    job.setCompany((String) company.get("display_name"));

                Map location = (Map) jobData.get("location");
                if (location != null)
                    job.setLocation((String) location.get("display_name"));

                job.setApplyLink((String) jobData.get("redirect_url"));

                try {
                job.setEmbedding(embeddingService.generateEmbedding(job.getDescription()));
            } catch (Exception e) {
                System.out.println("Embedding failed, saving job without embedding.");
            }
                jobRepository.save(job);
                imported++;
            }
        }
    }

    System.out.println("Imported jobs: " + imported);

    return "Imported " + imported + " jobs";
}
}