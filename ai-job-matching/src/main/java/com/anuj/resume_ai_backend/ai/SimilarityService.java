package com.anuj.resume_ai_backend.ai;

import org.springframework.stereotype.Service;

@Service
public class SimilarityService {

    public double cosineSimilarity(String v1, String v2) {

        if (v1 == null || v2 == null) return 0;

        String[] a = v1.split(",");
        String[] b = v2.split(",");

        int length = Math.min(a.length, b.length);

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < length; i++) {

            double x = Double.parseDouble(a[i]);
            double y = Double.parseDouble(b[i]);

            dotProduct += x * y;
            normA += x * x;
            normB += y * y;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}