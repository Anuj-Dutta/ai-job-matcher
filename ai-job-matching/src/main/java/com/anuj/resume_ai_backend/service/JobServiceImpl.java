package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.repository.JobRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    @Autowired
    private EmbeddingService embeddingService;

    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public Job saveJob(Job job) {

    String jobText =
            job.getTitle() + " " +
            job.getDescription() + " " +
            job.getSkills();

    String embedding = embeddingService.generateEmbedding(jobText);

    job.setEmbedding(embedding);

    return jobRepository.save(job);
}

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
}