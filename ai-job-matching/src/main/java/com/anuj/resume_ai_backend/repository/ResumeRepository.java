package com.anuj.resume_ai_backend.repository;

import com.anuj.resume_ai_backend.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
