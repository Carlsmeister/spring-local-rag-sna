package com.example.demo.controller;

import com.example.demo.ai.validation.FactualValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.lang.reflect.Method;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/max-size")
        public void triggerMaxSize() {
            throw new MaxUploadSizeExceededException(5 * 1024 * 1024);
        }

        @GetMapping("/test/illegal-argument")
        public void triggerIllegalArgument() {
            throw new IllegalArgumentException("Invalid input value");
        }

        @GetMapping("/test/factual-validation")
        public void triggerFactualValidation() {
            throw new FactualValidationException("Suggested rewrite contains hallucinated skills");
        }

        @GetMapping("/test/not-readable")
        public void triggerNotReadable() {
            throw new HttpMessageNotReadableException("Malformed JSON request body", (HttpInputMessage) null);
        }

        @GetMapping("/test/validation-error")
        public void triggerValidationError() throws Exception {
            Method method = TestController.class.getMethod("dummyMethod", Object.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "email", "Must be a valid email"));
            throw new MethodArgumentNotValidException(parameter, bindingResult);
        }

        @GetMapping("/test/runtime-error")
        public void triggerRuntimeError() {
            throw new RuntimeException("Sensitive database schema leak or absolute path /Users/carllundholm/Dokument/Projekt/local-ai");
        }

        public void dummyMethod(Object target) {
            // Dummy method for MethodParameter construction
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testHandleMaxSizeException() throws Exception {
        mockMvc.perform(get("/test/max-size"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File size exceeds the maximum limit of 5MB."));
    }

    @Test
    void testHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input value"));
    }

    @Test
    void testHandleFactualValidationException() throws Exception {
        mockMvc.perform(get("/test/factual-validation"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Suggested rewrite contains hallucinated skills"));
    }

    @Test
    void testHandleHttpMessageNotReadableException() throws Exception {
        mockMvc.perform(get("/test/not-readable"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request body"));
    }

    @Test
    void testHandleMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(get("/test/validation-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Must be a valid email"));
    }

    @Test
    void testHandleGeneralException() throws Exception {
        mockMvc.perform(get("/test/runtime-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An internal error occurred. Please try again."));
    }
}
