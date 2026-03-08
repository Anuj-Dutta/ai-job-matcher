package com.anuj.resume_ai_backend.entity;

import jakarta.persistence.*;

@Entity
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resumeText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String skillsJson;

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getResumeText() {
        return resumeText;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }
    @Lob
    @Column(columnDefinition = "TEXT")
    private String embedding;

    public String getEmbedding() {
    return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }
}