package com.example.demo.security;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PiiMaskingConverter extends MessageConverter {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*",
        Pattern.CASE_INSENSITIVE
    );

    // Matches standard phone numbers of varying lengths and notations
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\+?\\b\\d{1,4}[- .]?\\(?\\d{1,3}\\)?[- .]?\\d{1,4}[- .]?\\d{1,4}[- .]?\\d{1,9}\\b"
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null) {
            return null;
        }
        return maskPii(message);
    }

    public static String maskPii(String text) {
        if (text == null) {
            return null;
        }
        // Truncate excessively long log messages to protect against raw text dumps
        if (text.length() > 2000) {
            text = text.substring(0, 100) + " ... [TRUNCATED & REDACTED FOR SECURITY] ...";
        }
        // Mask emails
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            text = emailMatcher.replaceAll("[EMAIL_REDACTED]");
        }
        // Mask phone numbers
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            text = phoneMatcher.replaceAll("[PHONE_REDACTED]");
        }
        return text;
    }
}
