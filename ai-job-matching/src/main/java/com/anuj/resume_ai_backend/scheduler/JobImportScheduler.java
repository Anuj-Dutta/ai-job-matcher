package com.anuj.resume_ai_backend.scheduler;

import com.anuj.resume_ai_backend.service.AdzunaService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "jobs.import.scheduler.enabled", havingValue = "true")
public class JobImportScheduler {

    private final AdzunaService adzunaService;

    public JobImportScheduler(AdzunaService adzunaService) {
        this.adzunaService = adzunaService;
    }

    @Scheduled(
            fixedRateString = "${jobs.import.scheduler.fixed-rate-ms}",
            initialDelayString = "${jobs.import.scheduler.initial-delay-ms}"
    )
    public void importJobsAutomatically() {
        System.out.println("Auto-importing jobs from Adzuna...");
        String result = adzunaService.importJobs();
        System.out.println("Job import completed: " + result);
    }
}
