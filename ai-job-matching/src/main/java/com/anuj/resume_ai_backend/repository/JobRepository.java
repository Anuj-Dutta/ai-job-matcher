package com.anuj.resume_ai_backend.repository;

import com.anuj.resume_ai_backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByApplyLink(String applyLink);

    boolean existsByExternalId(String externalId);

    List<Job> findTop200BySkillsContainingIgnoreCase(String skill);


}