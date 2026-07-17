package com.example.demo.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonCleanUtilsTest {

    @Test
    void testCleanResponse_Null() {
        assertNull(JsonCleanUtils.cleanResponse(null));
    }

    @Test
    void testCleanResponse_Empty() {
        assertEquals("", JsonCleanUtils.cleanResponse(""));
        assertEquals("", JsonCleanUtils.cleanResponse("   "));
    }

    @Test
    void testCleanResponse_CleanJson() {
        String input = "{\"key\": \"value\"}";
        assertEquals(input, JsonCleanUtils.cleanResponse(input));
    }

    @Test
    void testCleanResponse_MarkdownWrapped() {
        String input = "```json\n{\"key\": \"value\"}\n```";
        assertEquals("{\"key\": \"value\"}", JsonCleanUtils.cleanResponse(input));
    }

    @Test
    void testCleanResponse_MarkdownWrappedWithoutJson() {
        String input = "```\n{\"key\": \"value\"}\n```";
        assertEquals("{\"key\": \"value\"}", JsonCleanUtils.cleanResponse(input));
    }

    @Test
    void testCleanResponse_ConversationalFluff() {
        String input = "Here is the response:\n```json\n{\"key\": \"value\"}\n```\nHope it helps!";
        assertEquals("{\"key\": \"value\"}", JsonCleanUtils.cleanResponse(input));
    }

    @Test
    void testCleanResponse_NoBraces() {
        String input = "No json here";
        assertEquals("No json here", JsonCleanUtils.cleanResponse(input));
    }

    @Test
    void testCleanResponse_NestedQuotesInArray() {
        String rawResponse = "{\n" +
                "  \"atsScore\": 70,\n" +
                "  \"strengths\": [\n" +
                "    \"Structured format\",\n" +
                "    \"Relevant skills listed in the \"Skills\" section\"\n" +
                "  ]\n" +
                "}";

        String expected = "{\n" +
                "  \"atsScore\": 70,\n" +
                "  \"strengths\": [\n" +
                "    \"Structured format\",\n" +
                "    \"Relevant skills listed in the \\\"Skills\\\" section\"\n" +
                "  ]\n" +
                "}";

        assertEquals(expected, JsonCleanUtils.cleanResponse(rawResponse));
    }

    @Test
    void testCleanResponse_NestedQuotesAndCommas() {
        String rawResponse = "{\n" +
                "  \"recommendations\": [\n" +
                "    \"We need \"clean, fast\" code\",\n" +
                "    \"Regular element\"\n" +
                "  ]\n" +
                "}";

        String expected = "{\n" +
                "  \"recommendations\": [\n" +
                "    \"We need \\\"clean, fast\\\" code\",\n" +
                "    \"Regular element\"\n" +
                "  ]\n" +
                "}";

        assertEquals(expected, JsonCleanUtils.cleanResponse(rawResponse));
    }

    @Test
    void testCleanResponse_AlreadyEscapedQuotes() {
        String rawResponse = "{\n" +
                "  \"strengths\": [\n" +
                "    \"Already \\\"escaped\\\" quote\"\n" +
                "  ]\n" +
                "}";

        String expected = "{\n" +
                "  \"strengths\": [\n" +
                "    \"Already \\\"escaped\\\" quote\"\n" +
                "  ]\n" +
                "}";

        assertEquals(expected, JsonCleanUtils.cleanResponse(rawResponse));
    }
}
