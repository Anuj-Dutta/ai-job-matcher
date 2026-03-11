package com.anuj.resume_ai_backend.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillExtractor {

    private static final List<String> SKILLS = Arrays.asList(
            "java",
            "spring",
            "spring boot",
            "python",
            "c",
            "c++",
            "javascript",
            "react",
            "node",
            "nodejs",
            "html",
            "css",
            "mysql",
            "mongodb",
            "git",
            "github",
            "docker",
            "kubernetes",
            "aws",
            "machine learning",
            "data structures",
            "algorithms",
            "linux"
    );

    public static List<String> extractSkills(String text) {
        String lower = text.toLowerCase();
        List<String> found = new ArrayList<>();

        for (String skill : SKILLS) {
            if (lower.contains(skill)) {
                found.add(skill);
            }
        }

        return found;
    }
}
