package com.example.demo.mapper;

/**
 * Utility class for cleaning raw AI responses before mapping them to JSON objects.
 * Handles unescaped nested quotes inside JSON string fields.
 */
public final class JsonCleanUtils {

    private JsonCleanUtils() {
        // Prevent instantiation
    }

    /**
     * Cleans raw AI response strings by removing markdown code block ticks, extracting
     * the valid JSON object structure, and escaping any unescaped nested double quotes.
     *
     * @param rawResponse the raw response from the AI model
     * @return the cleaned JSON string, or null if the input is null
     */
    public static String cleanResponse(String rawResponse) {
        if (rawResponse == null) {
            return null;
        }
        
        String cleaned = rawResponse.trim();

        // 1. Remove markdown block syntax if present
        if (cleaned.startsWith("```")) {
            // Remove opening ```json or ```
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            // Remove closing ```
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        cleaned = cleaned.trim();

        // 2. Extract valid JSON object if there's any surrounding fluff
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        // 3. Escape nested unescaped quotes inside JSON strings
        String escaped = escapeNestedQuotes(cleaned);

        // 4. Fix missing commas between JSON elements
        String fixedCommas = fixMissingCommas(escaped);

        // 5. Remove trailing commas in arrays/objects
        return removeTrailingCommas(fixedCommas);
    }

    /**
     * Scans the JSON string and escapes any double quotes that are not structural JSON tokens.
     */
    private static String escapeNestedQuotes(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        StringBuilder sb = new StringBuilder();
        int len = json.length();

        for (int i = 0; i < len; i++) {
            char c = json.charAt(i);
            if (c == '"') {
                // Check if this quote is structural
                if (isStructuralQuote(json, i)) {
                    sb.append('"');
                } else {
                    // Escape nested quote
                    sb.append("\\\"");
                }
            } else if (c == '\\' && i + 1 < len && json.charAt(i + 1) == '"') {
                // If it is already escaped, preserve it
                sb.append("\\\"");
                i++;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Determines if a quote character at a given index is a structural JSON token
     * (e.g. wrapping a key, wrapping a string value, next to structural braces/brackets/commas/colons).
     */
    private static boolean isStructuralQuote(String s, int index) {
        // Special case: first and last characters of the JSON string
        if (index == 0 || index == s.length() - 1) {
            return true;
        }

        int prevIdx = getPrevNonWhitespaceIndex(s, index);
        int nextIdx = getNextNonWhitespaceIndex(s, index);

        char prev = prevIdx != -1 ? s.charAt(prevIdx) : '\0';
        char next = nextIdx != -1 ? s.charAt(nextIdx) : '\0';

        // 1. Check if it's the opening quote of a key or value
        if (prev == '{' || prev == '[') {
            return true;
        }
        if (prev == ':') {
            // Check if the colon is structural (preceded by a key's closing double quote)
            int colonPrevIdx = getPrevNonWhitespaceIndex(s, prevIdx);
            if (colonPrevIdx != -1 && s.charAt(colonPrevIdx) == '"') {
                return true;
            }
        }

        // 2. Check if it's the closing quote of a key or value
        if (next == '}' || next == ']' || next == ':') {
            return true;
        }

        // 3. If prev is a comma, check if it's a structural comma separating elements
        if (prev == ',') {
            return isStructuralComma(s, prevIdx);
        }

        // 4. If next is a comma, check if it's a structural comma separating elements
        if (next == ',') {
            return isStructuralComma(s, nextIdx);
        }

        // 5. Check if it is a structural quote next to another quote separated by whitespace (missing comma case)
        if (next == '"' && nextIdx > index + 1) {
            return true;
        }
        if (prev == '"' && prevIdx < index - 1) {
            return true;
        }

        return false;
    }

    private static boolean isStructuralComma(String s, int commaIndex) {
        int prevIdx = getPrevNonWhitespaceIndex(s, commaIndex);
        int nextIdx = getNextNonWhitespaceIndex(s, commaIndex);

        char prev = prevIdx != -1 ? s.charAt(prevIdx) : '\0';
        char next = nextIdx != -1 ? s.charAt(nextIdx) : '\0';

        // A structural comma is preceded by a closing quote or digit/bracket/brace,
        // and followed by an opening quote or digit/bracket/brace (or closing bracket/brace for trailing commas).
        boolean validPrev = (prev == '"' || prev == ']' || prev == '}' || Character.isDigit(prev));
        boolean validNext = (next == '"' || next == '[' || next == '{' || Character.isDigit(next) || next == ']' || next == '}');

        return validPrev && validNext;
    }

    private static int getPrevNonWhitespaceIndex(String s, int index) {
        for (int i = index - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    private static int getNextNonWhitespaceIndex(String s, int index) {
        for (int i = index + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }

    private static String fixMissingCommas(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        StringBuilder sb = new StringBuilder();
        int len = json.length();
        boolean insideString = false;

        for (int i = 0; i < len; i++) {
            char c = json.charAt(i);

            if (c == '"') {
                // Check if this double quote is escaped
                boolean isEscaped = false;
                if (i > 0 && json.charAt(i - 1) == '\\') {
                    int bsCount = 0;
                    for (int j = i - 1; j >= 0; j--) {
                        if (json.charAt(j) == '\\') {
                            bsCount++;
                        } else {
                            break;
                        }
                    }
                    isEscaped = (bsCount % 2 != 0);
                }

                if (isEscaped) {
                    sb.append(c);
                } else {
                    sb.append(c);
                    insideString = !insideString;

                    if (!insideString) {
                        // We just closed a string! Look ahead to see if the next non-whitespace character starts a new element
                        int nextIdx = getNextNonWhitespaceIndex(json, i);
                        if (nextIdx != -1) {
                            char nextChar = json.charAt(nextIdx);
                            // JSON values/keys can only start with: ", {, [, digit, -, t, f, n
                            boolean isNextValueStart = (nextChar == '"' || nextChar == '{' || nextChar == '[' 
                                    || Character.isDigit(nextChar) || nextChar == '-' 
                                    || nextChar == 't' || nextChar == 'f' || nextChar == 'n');

                            if (isNextValueStart) {
                                // Check if there is already a comma between the closing quote and next element start
                                boolean hasComma = false;
                                for (int k = i + 1; k < nextIdx; k++) {
                                    if (json.charAt(k) == ',') {
                                        hasComma = true;
                                        break;
                                    }
                                }
                                if (!hasComma) {
                                    sb.append(",");
                                }
                            }
                        }
                    }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String removeTrailingCommas(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        StringBuilder sb = new StringBuilder();
        int len = json.length();
        boolean insideString = false;

        for (int i = 0; i < len; i++) {
            char c = json.charAt(i);

            if (c == '"') {
                // Check if this double quote is escaped
                boolean isEscaped = false;
                if (i > 0 && json.charAt(i - 1) == '\\') {
                    int bsCount = 0;
                    for (int j = i - 1; j >= 0; j--) {
                        if (json.charAt(j) == '\\') {
                            bsCount++;
                        } else {
                            break;
                        }
                    }
                    isEscaped = (bsCount % 2 != 0);
                }

                sb.append(c);
                if (!isEscaped) {
                    insideString = !insideString;
                }
            } else if (c == ',' && !insideString) {
                // Check if the next non-whitespace character is } or ]
                int nextIdx = getNextNonWhitespaceIndex(json, i);
                if (nextIdx != -1) {
                    char nextChar = json.charAt(nextIdx);
                    if (nextChar == '}' || nextChar == ']') {
                        // Skip appending this trailing comma!
                        continue;
                    }
                }
                sb.append(c);
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
