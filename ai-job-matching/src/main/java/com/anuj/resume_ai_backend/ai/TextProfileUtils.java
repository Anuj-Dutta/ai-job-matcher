package com.anuj.resume_ai_backend.ai;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class TextProfileUtils {

    private static final Pattern NON_TOKEN_PATTERN = Pattern.compile("[^a-z0-9+#./-]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is",
            "it", "its", "of", "on", "or", "that", "the", "their", "this", "to", "was", "were", "will",
            "with", "you", "your", "we", "our", "they", "them", "using", "use", "used", "into",
            "than", "then", "also", "over", "under", "such", "within", "across", "through", "per", "via"
    );
    private static final Set<String> SHORT_TOKEN_ALLOWLIST = Set.of(
            "c", "r", "go", "ui", "ux", "qa", "hr", "bi", "ml", "ai"
    );

    private TextProfileUtils() {
    }

    public static String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = NON_TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT)).replaceAll(" ");
        return normalized.trim().replaceAll("\\s+", " ");
    }

    public static Set<String> tokenize(String text) {
        String normalized = normalizeText(text);
        if (normalized.isEmpty()) {
            return Collections.emptySet();
        }

        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        for (String token : normalized.split(" ")) {
            if (token.isBlank() || STOP_WORDS.contains(token)) {
                continue;
            }
            if (token.length() <= 2 && !SHORT_TOKEN_ALLOWLIST.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    public static Set<String> parseSkills(String rawSkills) {
        if (rawSkills == null || rawSkills.isBlank()) {
            return Collections.emptySet();
        }

        String sanitized = rawSkills
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");

        LinkedHashSet<String> skills = new LinkedHashSet<>();
        for (String value : sanitized.split(",")) {
            String normalized = normalizeText(value);
            if (!normalized.isEmpty()) {
                skills.add(normalized);
            }
        }
        return skills;
    }

    public static String joinSkills(Set<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        return String.join(", ", skills);
    }

    public static double overlapScore(Set<String> left, Set<String> right) {
        if (left == null || left.isEmpty() || right == null || right.isEmpty()) {
            return 0.0;
        }

        int overlap = 0;
        for (String token : left) {
            if (right.contains(token)) {
                overlap++;
            }
        }

        if (overlap == 0) {
            return 0.0;
        }

        return (2.0 * overlap) / (left.size() + right.size());
    }

    public static List<String> topKeywords(String text, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        String normalized = normalizeText(text);
        if (normalized.isBlank()) {
            return Collections.emptyList();
        }

        Map<String, Integer> counts = new HashMap<>();
        for (String token : normalized.split(" ")) {
            if (token.isBlank() || STOP_WORDS.contains(token)) {
                continue;
            }
            if (token.length() <= 2 && !SHORT_TOKEN_ALLOWLIST.contains(token)) {
                continue;
            }
            counts.merge(token, 1, Integer::sum);
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    public static List<String> buildSearchQueries(String resumeText, String skillsCsv) {
        LinkedHashSet<String> queries = new LinkedHashSet<>();

        for (String skill : parseSkills(skillsCsv)) {
            queries.add(skill);
            queries.add(skill + " jobs");
        }

        List<String> keywords = topKeywords(resumeText, 8);
        for (String keyword : keywords) {
            if (keyword.length() >= 4) {
                queries.add(keyword);
            }
        }

        addRoleQueries(resumeText, queries);

        return queries.stream()
                .filter(query -> !query.isBlank())
                .limit(10)
                .toList();
    }

    private static void addRoleQueries(String resumeText, Set<String> queries) {
        String text = normalizeText(resumeText);
        if (text.isBlank()) {
            return;
        }

        Map<String, List<String>> rolePatterns = Map.ofEntries(
                Map.entry("software", List.of("software engineer", "developer", "full stack developer")),
                Map.entry("java", List.of("java developer", "backend developer")),
                Map.entry("python", List.of("python developer", "data analyst")),
                Map.entry("data", List.of("data analyst", "data engineer", "data scientist")),
                Map.entry("support", List.of("customer support", "customer success", "technical support")),
                Map.entry("sales", List.of("sales executive", "business development")),
                Map.entry("marketing", List.of("digital marketing", "seo specialist", "content writer")),
                Map.entry("finance", List.of("accountant", "financial analyst", "bookkeeping")),
                Map.entry("accounting", List.of("accountant", "bookkeeping")),
                Map.entry("hr", List.of("hr executive", "recruiter", "talent acquisition")),
                Map.entry("recruitment", List.of("recruiter", "talent acquisition")),
                Map.entry("design", List.of("graphic designer", "ui ux designer")),
                Map.entry("healthcare", List.of("healthcare assistant", "nurse", "pharmacist")),
                Map.entry("teaching", List.of("teacher", "education counselor")),
                Map.entry("education", List.of("teacher", "education counselor")),
                Map.entry("qa", List.of("qa engineer", "automation tester")),
                Map.entry("testing", List.of("qa engineer", "automation tester")),
                Map.entry("project", List.of("project manager", "business analyst")),
                Map.entry("product", List.of("product manager", "business analyst"))
        );

        for (Map.Entry<String, List<String>> entry : rolePatterns.entrySet()) {
            if (text.contains(entry.getKey())) {
                queries.addAll(entry.getValue());
            }
        }
    }
}
