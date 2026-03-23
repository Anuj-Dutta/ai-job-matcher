package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.ai.SkillExtractor;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final EmbeddingService embeddingService;
    private final Tika tika = new Tika();

    public ResumeServiceImpl(ResumeRepository resumeRepository, EmbeddingService embeddingService) {
        this.resumeRepository = resumeRepository;
        this.embeddingService = embeddingService;
    }

    @Override
    public Resume saveResume(String email, MultipartFile file) throws Exception {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required.");
        }

        String text = tika.parseToString(file.getInputStream());

        Resume resume = new Resume();
        resume.setEmail(email.trim());
        resume.setResumeText(text);
        resume.setSkillsJson(SkillExtractor.extractSkillsAsCsv(text));
        resume.setEmbedding(embeddingService.generateEmbedding(text));

        System.out.println("Embedding generated for resume: " + (resume.getEmbedding() != null));
        return resumeRepository.save(resume);
    }
}
