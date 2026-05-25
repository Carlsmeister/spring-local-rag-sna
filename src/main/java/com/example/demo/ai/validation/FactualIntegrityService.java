package com.example.demo.ai.validation;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FactualIntegrityService {

    private static final Set<String> COMMON_RESUME_VOCABULARY = new HashSet<>(Arrays.asList(
        "engineered", "optimized", "maximized", "speeds", "uptime", "systems", "action", "oriented",
        "metric", "driven", "concise", "managed", "led", "created", "developed", "built", "designed",
        "streamlined", "improved", "increased", "reduced", "saved", "delivered", "coordinated",
        "implemented", "facilitated", "formulated", "enhanced", "accelerated", "boosted", "generated",
        "database", "connections", "optimization", "scalability", "uptime", "performance", "response",
        "efficiency", "integration", "deployment", "infrastructure", "development", "application",
        "responsible", "managing", "handling", "working", "using", "with", "through", "highly", "robust",
        "strong", "clean", "secure", "scalable", "advanced", "modern", "speed", "latency", "throughput",
        "across", "production", "uptime", "speed", "speeds", "uptime", "system", "systems", "high",
        "quality", "code", "architecture", "design", "team", "project", "projects", "maintenance",
        "maintain", "maintaining", "scale", "scaling", "fast", "faster", "secure", "securing", "safety",
        "efficiency", "effective", "effectively", "maximize", "maximizes", "minimizes", "minimize",
        "duration", "process", "processes", "workflow", "workflows", "pipeline", "pipelines", "flow",
        "flows", "task", "tasks", "issue", "issues", "bug", "bugs", "resolving", "resolved", "resolve",
        "fixing", "fixed", "fix", "testing", "tested", "test", "tests", "unit", "units", "mocking",
        "verification", "verify", "verifying", "documentation", "document", "documents", "documenting",
        "reporting", "report", "reports", "analyzing", "analyzed", "analysis", "matching", "matched",
        "suggesting", "suggested", "suggestion", "suggestions", "rewrite", "rewriting", "rewritten",
        "sentence", "bullet", "point", "points"
    ));

    private final Set<String> stopWords = new HashSet<>(Arrays.asList(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
        "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
        "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
        "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
        "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself",
        "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is",
        "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself",
        "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours",
        "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should",
        "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them",
        "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've",
        "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we",
        "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where",
        "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would",
        "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"
    ));

    /**
     * Validates that the generated suggestion does not invent any factual credentials,
     * proper nouns, or numeric metrics not present in the original sentence or keywords.
     *
     * @param originalText the candidate's original sentence
     * @param keywords the list of target keywords allowed to be incorporated
     * @param suggestion the AI's generated suggestion text
     * @throws FactualValidationException if factual fabrications are detected
     */
    public void validateFactualIntegrity(String originalText, List<String> keywords, String suggestion) {
        if (originalText == null || suggestion == null) {
            throw new IllegalArgumentException("Original text and suggestion must not be null");
        }

        // 1. Audit Numbers / Metrics
        List<String> originalNumbers = extractNumbers(originalText);
        List<String> suggestedNumbers = extractNumbers(suggestion);
        for (String num : suggestedNumbers) {
            if (!originalNumbers.contains(num)) {
                throw new FactualValidationException("Hallucination detected: Fabricated numeric metric '" + num + "' was added.");
            }
        }

        // 2. Audit Proper Nouns / Skills / Certifications
        List<String> suggestedWords = tokenizeWords(suggestion);
        Set<String> originalWordsLower = new HashSet<>(tokenizeWords(originalText.toLowerCase()));
        if (keywords != null) {
            for (String kw : keywords) {
                originalWordsLower.addAll(tokenizeWords(kw.toLowerCase()));
            }
        }

        for (int i = 0; i < suggestedWords.size(); i++) {
            String word = suggestedWords.get(i);
            String wordLower = word.toLowerCase();

            // If the word isn't in original text, keywords, stop words, or resume vocabulary whitelist
            if (!originalWordsLower.contains(wordLower)
                    && !stopWords.contains(wordLower)
                    && !COMMON_RESUME_VOCABULARY.contains(wordLower)) {

                // If it is capitalized, it represents a potential fabricated skill, tool, company, or certification
                // Note: we skip checking capitalization on the first word of the sentence to prevent sentence-start false positives,
                // except if the word itself is highly suspicious or fully uppercase (e.g. AWS).
                boolean isFirstWord = (i == 0);
                boolean isFullyUppercase = word.toUpperCase().equals(word) && word.length() > 1;

                if (Character.isUpperCase(word.charAt(0)) && (!isFirstWord || isFullyUppercase)) {
                    throw new FactualValidationException("Hallucination detected: Fabricated proper noun or term '" + word + "' was added.");
                }
            }
        }
    }

    private List<String> extractNumbers(String text) {
        List<String> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\b\\d+(?:[kKmMbB]|%|\\b)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    private List<String> tokenizeWords(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\b[a-zA-Z-]+\\b");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            words.add(matcher.group());
        }
        return words;
    }
}
