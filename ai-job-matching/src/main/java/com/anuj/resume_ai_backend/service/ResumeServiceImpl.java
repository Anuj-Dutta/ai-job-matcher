package com.anuj.resume_ai_backend.service;
import com.anuj.resume_ai_backend.ai.EmbeddingService;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.ResumeRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.anuj.resume_ai_backend.ai.SkillExtractor;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final EmbeddingService embeddingService;

    public ResumeServiceImpl(ResumeRepository resumeRepository,EmbeddingService embeddingService) {
    this.resumeRepository = resumeRepository;
    this.embeddingService = embeddingService;
    }

    @Override
    public Resume saveResume(String email, MultipartFile file) throws Exception {

        Tika tika = new Tika();
        String text = tika.parseToString(file.getInputStream());

        Resume resume = new Resume();
        resume.setEmail(email);
        resume.setResumeText(text);
        resume.setSkillsJson(SkillExtractor.extractSkills(text).toString());
        resume.setEmbedding(embeddingService.generateEmbedding(text));
        System.out.println("EMBEDDING GENERATED: " + resume.getEmbedding());

        return resumeRepository.save(resume);
    }
}