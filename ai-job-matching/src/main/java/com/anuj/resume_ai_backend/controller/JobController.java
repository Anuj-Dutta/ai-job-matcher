package com.anuj.resume_ai_backend.controller;

import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping("/add")
    public Job addJob(@RequestBody Job job) {
        return jobService.saveJob(job);
    }

    @GetMapping("/all")
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }
}