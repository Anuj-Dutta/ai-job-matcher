package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.entity.Job;

import java.util.List;

public interface JobService {

    Job saveJob(Job job);

    List<Job> getAllJobs();
}
