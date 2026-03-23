package com.anuj.resume_ai_backend.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class TextProfileUtils {

    private static final Pattern NON_TOKEN_PATTERN = Pattern.compile("[^a-z0-9+#./-]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is",
            "it", "its", "of", "on", "or", "that", "the", "their", "this", "to", "was", "were", "will",
            "with", "you", "your", "we", "our", "they", "them", "with", "using", "use", "used", "into",
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

    public static List<String> tokenizeToList(String text) {
        return Arrays.asList(normalizeText(text).split(" "));
    }
}
