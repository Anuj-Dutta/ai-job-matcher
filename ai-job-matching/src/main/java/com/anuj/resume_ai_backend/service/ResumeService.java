package com.anuj.resume_ai_backend.service;

import com.anuj.resume_ai_backend.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    Resume saveResume(String email, MultipartFile file) throws Exception;

}
