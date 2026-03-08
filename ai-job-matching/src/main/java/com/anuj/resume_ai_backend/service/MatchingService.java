package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.entity.Resume;

import java.util.List;

public interface MatchingService {

    List<Job> matchJobs(Resume resume);

}