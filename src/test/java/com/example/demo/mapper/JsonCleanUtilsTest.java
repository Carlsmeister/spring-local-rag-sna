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
}
