package com.anuj.resume_ai_backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String company;

    private String location;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String skills;

    private String applyLink;


    @Column(unique = true)
private String externalId;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "TEXT")
    private String embedding;


    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getSkills() {
        return skills;
    }

    public String getApplyLink() {
        return applyLink;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setApplyLink(String applyLink) {
        this.applyLink = applyLink;
    }
    @Transient
    private double matchScore;

    public double getMatchScore() {
    return matchScore;
    }

    public void setMatchScore(double matchScore) {
    this.matchScore = matchScore;
    }

    public String getEmbedding() {
    return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public String getExternalId() {
    return externalId;
}

public void setExternalId(String externalId) {
    this.externalId = externalId;
}
}