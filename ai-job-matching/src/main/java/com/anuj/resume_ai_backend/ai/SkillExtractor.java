package com.anuj.resume_ai_backend.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SkillExtractor {

    private static final List<String> SKILLS = Arrays.asList(
            "java", "spring", "spring boot", "hibernate", "microservices", "python", "django", "flask",
            "fastapi", "c", "c++", "c#", ".net", "javascript", "typescript", "react", "angular", "vue",
            "next.js", "node", "nodejs", "express", "html", "css", "tailwind", "bootstrap", "mysql",
            "postgresql", "sql", "mongodb", "redis", "oracle", "power bi", "tableau", "excel", "git",
            "github", "gitlab", "docker", "kubernetes", "aws", "azure", "gcp", "terraform", "jenkins",
            "ci/cd", "linux", "rest api", "graphql", "machine learning", "deep learning", "nlp",
            "computer vision", "data science", "data analysis", "data engineering", "etl", "spark",
            "hadoop", "airflow", "pandas", "numpy", "scikit-learn", "tensorflow", "pytorch", "statistics",
            "data structures", "algorithms", "testing", "junit", "selenium", "playwright", "manual testing",
            "automation testing", "qa", "project management", "agile", "scrum", "jira", "product management",
            "business analysis", "requirements gathering", "stakeholder management", "sales", "crm",
            "lead generation", "digital marketing", "seo", "sem", "content marketing", "social media",
            "customer support", "customer success", "operations", "supply chain", "procurement", "finance",
            "accounting", "financial analysis", "bookkeeping", "hr", "recruitment", "talent acquisition",
            "graphic design", "figma", "ui design", "ux design", "adobe photoshop", "adobe illustrator",
            "healthcare", "nursing", "clinical", "pharmacy", "teaching", "education"
    );

    public static List<String> extractSkills(String text) {
        String normalizedText = " " + TextProfileUtils.normalizeText(text) + " ";
        Set<String> found = new LinkedHashSet<>();

        for (String skill : SKILLS) {
            String normalizedSkill = TextProfileUtils.normalizeText(skill);
            if (!normalizedSkill.isEmpty() && normalizedText.contains(" " + normalizedSkill + " ")) {
                found.add(skill);
            }
        }

        return new ArrayList<>(found);
    }

    public static String extractSkillsAsCsv(String text) {
        return TextProfileUtils.joinSkills(new LinkedHashSet<>(extractSkills(text)));
    }
}
