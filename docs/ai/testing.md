# AI Workstation: Testing Strategy

This document defines the testing strategy, testing layers, test environments, and negative test matrices for the **local-ai** application.

---

## 1. Multi-Tiered Testing Strategy

To maintain high development velocity and codebase reliability, we utilize a tiered testing pyramid that isolates slow, resource-heavy local LLM operations from fast, standard build executions.

```
+-------------------------------------------------------+
|                System Integration Tests               |  <- Optional (live DB, live PGVector)
|                    (Web Smoke Tests)                  |
+-------------------------------------------------------+
|                Ollama Integration Tests               |  <- Optional (Requires active local Ollama,
|               (Model Response Quality)                |     runs only under specific profile)
+-------------------------------------------------------+
|             Controller Contract Tests                 |  <- Standard (Uses MockMvc, mocks out services,
|                 (@WebMvcTest Layer)                   |     asserts status codes & JSON models)
+-------------------------------------------------------+
|              Isolated Service Unit Tests              |  <- Standard (Standard Mockito, mocks out DB
|              (Business Logic & Mock LLM)              |     repositories & ChatModel clients)
+-------------------------------------------------------+
```

---

## 2. Execution Environments

### Standard CI Build Loop (`mvn clean test`)
- **Strict Rule**: Normal builds **MUST NOT** require a live, running Ollama instance, a running PostgreSQL instance, or external web connections.
- **Mock Enforcement**: All `ChatModel`, `VectorStore`, and database repositories must be fully mocked out using standard Mockito features.

### Local Quality/AI Test Loop (`mvn test -Pintegration-test`)
- Runs only when the `integration-test` Maven profile is explicitly enabled.
- Requires local Ollama running with the target `gemma4:2eb` model.
- Validates the real model response accuracy, schema parsing, and database transactions.

---

## 3. Standard Mocking & Fixtures

### Mocking the Chat Client
When writing service-level tests, Mockito can simulate structured AI responses:
```java
@ExtendWith(MockitoExtension.class)
class CvAnalysisServiceTest {
    @Mock
    private ChatModel chatModel;
    
    @Test
    void testAnalysisSuccess() {
        String mockResponse = "{\"atsScore\": 80, \"strengths\": [], \"weaknesses\": [], \"recommendations\": []}";
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(...));
        // Run service-level tests cleanly in milliseconds.
    }
}
```

### Reusable Test Fixtures
All reusable test documents must be kept in standard resource paths:
- `src/test/resources/fixtures/sample_resume_standard.pdf` (Simple, clean layout)
- `src/test/resources/fixtures/sample_job_description.txt` (Standard job ad)
- `src/test/resources/fixtures/prompt_injection_resume.pdf` (Malicious prompt payload)

---

## 4. Negative Test Matrix & Guardrail Verifications

We maintain a strict set of assertions to guarantee system resilience:

| Category | Target Vulnerability | Test Assertions |
| :--- | :--- | :--- |
| **File Validation** | Uploading malformed executable renamed to `.pdf` | Expect `InvalidFileFormatException` and `400 Bad Request`. |
| **File Validation** | Uploading file exceeding **5MB** | Expect `MaxUploadSizeExceededException` and `413 Payload Too Large`. |
| **File Validation** | Uploading completely empty file | Expect `EmptyDocumentException` and `400 Bad Request`. |
| **AI Validation** | Model generates malformed/incomplete JSON response | Confirm fallback parses gracefully or throws validation exceptions instead of leaking raw text. |
| **AI Validation** | Model injects fabricated AWS cert in rewrite suggestions | Confirm validation checks intercept, reject, and return a clean error. |
| **AI Validation** | Prompt injection commands within resume | Confirm system prompt delimiters neutralized the input and the model ignored instructions. |
| **Security Boundaries**| Unauthorized requests to API endpoints | Expect `401 Unauthorized` or `403 Forbidden` (once auth is active). |
| **API Contract** | Sending null request payloads | Expect `MethodArgumentNotValidException` and `400 Bad Request`. |
