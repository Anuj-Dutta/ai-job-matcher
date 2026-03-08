package com.anuj.resume_ai_backend.controller;

import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.service.ResumeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public Resume uploadResume(
            @RequestParam String email,
            @RequestParam MultipartFile file
    ) throws Exception {

        return resumeService.saveResume(email, file);
    }
}