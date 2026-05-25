package com.example.demo.security;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class PiiMaskingConverterTest {

    @Test
    void testMaskPiiNullInput() {
        assertNull(PiiMaskingConverter.maskPii(null));
    }

    @Test
    void testMaskPiiNoPii() {
        String input = "This is a normal log message without any PII.";
        assertEquals(input, PiiMaskingConverter.maskPii(input));
    }

    @Test
    void testMaskEmails() {
        String input1 = "Contact me at john.doe@example.com for info.";
        assertEquals("Contact me at [EMAIL_REDACTED] for info.", PiiMaskingConverter.maskPii(input1));

        String input2 = "Emails: test-email_123@sub.domain.co.uk and dummy@domain.org";
        assertEquals("Emails: [EMAIL_REDACTED] and [EMAIL_REDACTED]", PiiMaskingConverter.maskPii(input2));
    }

    @Test
    void testMaskPhoneNumbers() {
        String input1 = "My phone number is +46 70-123 45 67.";
        // +46 70-123 45 67 matches phone pattern
        assertEquals("My phone number is [PHONE_REDACTED].", PiiMaskingConverter.maskPii(input1));

        String input2 = "Call 0701234567 or +1 (555) 019-2834.";
        // Note: The phone regex: "\\+?\\b\\d{1,4}[- .]?\\(?\\d{1,3}\\)?[- .]?\\d{1,4}[- .]?\\d{1,4}[- .]?\\d{1,9}\\b"
        // Let's verify how it handles various patterns or just redact what matches
        String masked = PiiMaskingConverter.maskPii(input2);
        // It should contain redacting tags
        assertEquals("Call [PHONE_REDACTED] or [PHONE_REDACTED].", masked);
    }

    @Test
    void testTruncateLongLogs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 205; i++) {
            sb.append("1234567890"); // 2050 characters
        }
        String input = sb.toString();
        String result = PiiMaskingConverter.maskPii(input);
        
        assertEquals(100 + " ... [TRUNCATED & REDACTED FOR SECURITY] ...".length(), result.length());
        assertEquals(input.substring(0, 100) + " ... [TRUNCATED & REDACTED FOR SECURITY] ...", result);
    }

    @Test
    void testConvertWithLoggingEvent() {
        ILoggingEvent mockEvent = Mockito.mock(ILoggingEvent.class);
        when(mockEvent.getFormattedMessage()).thenReturn("User email is admin@company.com");

        PiiMaskingConverter converter = new PiiMaskingConverter();
        String result = converter.convert(mockEvent);

        assertEquals("User email is [EMAIL_REDACTED]", result);
    }

    @Test
    void testConvertWithNullLoggingEventMessage() {
        ILoggingEvent mockEvent = Mockito.mock(ILoggingEvent.class);
        when(mockEvent.getFormattedMessage()).thenReturn(null);

        PiiMaskingConverter converter = new PiiMaskingConverter();
        assertNull(converter.convert(mockEvent));
    }
}
