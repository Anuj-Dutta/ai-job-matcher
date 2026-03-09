package com.anuj.resume_ai_backend.scheduler;

import com.anuj.resume_ai_backend.service.AdzunaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobImportScheduler {

    private final AdzunaService adzunaService;

    public JobImportScheduler(AdzunaService adzunaService) {
        this.adzunaService = adzunaService;
    }

    @Scheduled(fixedRate = 360000) // every 6 minutes
    public void importJobsAutomatically() {

        System.out.println("Auto-importing jobs from Adzuna...");

        adzunaService.importJobs();

        System.out.println("Job import completed.");

    }
}