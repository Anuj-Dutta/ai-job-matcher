package com.anuj.resume_ai_backend.ai;

import org.springframework.stereotype.Service;

@Service
public class SimilarityService {

    public double cosineSimilarity(String v1, String v2) {
        if (v1 == null || v2 == null || v1.isBlank() || v2.isBlank()) {
            return 0;
        }

        String[] a = v1.split(",");
        String[] b = v2.split(",");
        int length = Math.min(a.length, b.length);

        if (length == 0) {
            return 0;
        }

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        try {
            for (int i = 0; i < length; i++) {
                double x = Double.parseDouble(a[i].trim());
                double y = Double.parseDouble(b[i].trim());

                dotProduct += x * y;
                normA += x * x;
                normB += y * y;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid vector value encountered while calculating similarity.");
            return 0;
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        if (Double.isNaN(similarity) || Double.isInfinite(similarity)) {
            return 0;
        }

        return similarity;
    }
}
