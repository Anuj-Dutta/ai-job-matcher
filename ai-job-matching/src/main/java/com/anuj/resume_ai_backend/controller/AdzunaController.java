package com.anuj.resume_ai_backend.controller;

import com.anuj.resume_ai_backend.service.AdzunaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adzuna")
public class AdzunaController {

    private final AdzunaService adzunaService;

    public AdzunaController(AdzunaService adzunaService) {
        this.adzunaService = adzunaService;
    }

    @GetMapping("/test")
    public String testAdzuna() {
        return adzunaService.importJobs();
    }
}