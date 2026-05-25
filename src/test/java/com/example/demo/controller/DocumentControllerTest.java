package com.example.demo.controller;

import com.example.demo.document.service.DocumentService;
import com.example.demo.dto.UploadResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({DocumentController.class, GlobalExceptionHandler.class})
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    @Test
    @WithMockUser
    void testUploadEmptyFileReturnsBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/documents/upload")
                .file(emptyFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("File must not be empty"));
    }

    @Test
    @WithMockUser
    void testUploadInvalidExtensionReturnsBadRequest() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "malicious.exe", "application/octet-stream", "fake-exe-bytes".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                .file(invalidFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid file type. Only PDF and DOCX files are allowed."));
    }

    @Test
    @WithMockUser
    void testUploadFileSizeExceededReturnsBadRequest() throws Exception {
        byte[] largeBytes = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.pdf", "application/pdf", largeBytes);

        mockMvc.perform(multipart("/api/documents/upload")
                .file(largeFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("File size exceeds the maximum limit of 5MB."));
    }

    @Test
    @WithMockUser
    void testUploadSuccessReturnsAccepted() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "my_resume.pdf", "application/pdf", "dummy PDF raw contents".getBytes());
        UUID docId = UUID.randomUUID();
        UploadResponseDto mockResponse = new UploadResponseDto(
                docId,
                "my_resume.pdf",
                validFile.getSize(),
                "application/pdf",
                "Parsed CV Content which is definitely at least 100 characters long to satisfy the text extraction minimum boundary rules.",
                LocalDateTime.now()
        );

        when(documentService.uploadAndParseDocument(anyString(), anyLong(), anyString(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/documents/upload")
                .file(validFile)
                .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documentId").value(docId.toString()))
                .andExpect(jsonPath("$.fileName").value("my_resume.pdf"))
                .andExpect(jsonPath("$.extractedText").value(mockResponse.extractedText()));
    }

    @Test
    @WithMockUser
    void testUploadIoExceptionReturnsInternalServerError() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "my_resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "dummy DOCX content".getBytes());

        when(documentService.uploadAndParseDocument(anyString(), anyLong(), anyString(), any()))
                .thenThrow(new IOException("Disk read error"));

        mockMvc.perform(multipart("/api/documents/upload")
                .file(validFile)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("An internal error occurred. Please try again."));
    }

    @Test
    @WithMockUser
    void testUploadMagicByteMismatchReturnsBadRequest() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "spoofed.pdf", "application/pdf", "fake-pdf".getBytes());

        when(documentService.uploadAndParseDocument(anyString(), anyLong(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Invalid file type. Only PDF and DOCX files are allowed."));

        mockMvc.perform(multipart("/api/documents/upload")
                .file(validFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid file type. Only PDF and DOCX files are allowed."));
    }

    @Test
    @WithMockUser
    void testUploadTooShortParsedTextReturnsBadRequest() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "short.pdf", "application/pdf", "short text".getBytes());

        when(documentService.uploadAndParseDocument(anyString(), anyLong(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Extracted text must be at least 100 characters."));

        mockMvc.perform(multipart("/api/documents/upload")
                .file(validFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Extracted text must be at least 100 characters."));
    }
}

