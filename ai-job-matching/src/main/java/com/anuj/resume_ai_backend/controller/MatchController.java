package com.anuj.resume_ai_backend.controller;

import com.anuj.resume_ai_backend.entity.Job;
import com.anuj.resume_ai_backend.entity.Resume;
import com.anuj.resume_ai_backend.repository.ResumeRepository;
import com.anuj.resume_ai_backend.service.EmailDeliveryResult;
import com.anuj.resume_ai_backend.service.EmailService;
import com.anuj.resume_ai_backend.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/match")
public class MatchController {

    private final ResumeRepository resumeRepository;
    private final MatchingService matchingService;
    private final EmailService emailService;

    public MatchController(
            ResumeRepository resumeRepository,
            MatchingService matchingService,
            EmailService emailService
    ) {
        this.resumeRepository = resumeRepository;
        this.matchingService = matchingService;
        this.emailService = emailService;
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<List<Job>> matchJobs(@PathVariable Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow();
        List<Job> jobs = matchingService.matchJobs(resume);

        StringBuilder body = new StringBuilder("Top matched jobs:\n\n");
        for (Job job : jobs) {
            body.append(job.getTitle())
                    .append(" - ")
                    .append(job.getCompany())
                    .append(" (score: ")
                    .append(job.getMatchScore())
                    .append(")\n")
                    .append(job.getApplyLink())
                    .append("\n\n");
        }

        EmailDeliveryResult deliveryResult = emailService.sendEmail(resume.getEmail(), "Your Job Matches", body.toString());

        return ResponseEntity.ok()
                .header("X-Email-Status", deliveryResult.status())
                .header("X-Email-Message", deliveryResult.message())
                .body(jobs);
    }
}
