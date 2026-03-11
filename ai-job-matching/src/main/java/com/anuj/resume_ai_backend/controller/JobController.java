package com.anuj.resume_ai_backend.controller;

import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/add")
    public Job addJob(@RequestBody Job job) {
        return jobService.saveJob(job);
    }

    @GetMapping("/all")
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @PostMapping("/backfill-embeddings")
    public Map<String, Integer> backfillEmbeddings() {
        int updatedJobs = jobService.backfillMissingEmbeddings();
        return Map.of("updatedJobs", updatedJobs);
    }
}
