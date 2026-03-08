package com.anuj.resume_ai_backend.service;

import java.util.List;

import com.anuj.resume_ai_backend.entity.Job;

public interface JobService {

    Job saveJob(Job job);
    List<Job> getAllJobs();
}