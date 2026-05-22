# AI Workstation: Active Task Board

This task board acts as the active work tracker for the **local-ai** project.

## Board Status Summary
- **Current Active Phase**: Phase 0 (Foundation) & Phase 1 (Document Upload & Text Extraction)
- **Active Sprint Target**: Verify local connection to Ollama and enable file parsing via Apache Tika.

---

## Rules for Updating Tasks
1. **Status Progression**: Tasks move from `Backlog` -> `Next Actions` -> `In Progress` -> `Done` (or `Blocked` if dependencies are missing).
2. **Phase Alignment**: Implement only the tasks aligned with the current active phase. Do not jump ahead to build abstractions for later phases.
3. **No Code Without Tasks**: Do not create or edit source code files unless an open task exists on this board or a parent roadmap phase is approved.
4. **Marking Progress**: Use `[ ]` for uncompleted tasks, `[/]` for in-progress tasks, and `[x]` for completed tasks.

---

## Active Task Details

### TS-001
- **Title**: Verify Ollama Chat Connection
- **Phase**: Phase 0
- **Status**: Next Actions
- **Description**: Implement a simple controller endpoint `/api/chat` that queries the local Ollama instance (`gemma4:2eb` model) using Spring AI `ChatModel` to confirm the local setup is correct.
- **Files likely affected**: 
  - `src/main/java/com/example/demo/controller/ChatTestController.java`
  - `src/main/resources/application.properties`
- **Acceptance Criteria**:
  - GET request to `/api/chat?message=Ping` returns a clean, non-null string containing the model's text response.
- **Tests required**: 
  - `ChatTestControllerIntegrationTest` (with mock configuration fallback for default CI runs).
- **Notes**: Ollama must be running on the developer workstation during test verification.

### TS-002
- **Title**: Review Current Project Dependencies
- **Phase**: Phase 0
- **Status**: Next Actions
- **Description**: Audit the active dependencies in `pom.xml` to ensure Spring AI, Security, Lombok, and Tika are properly imported, and verify the main application class successfully boots.
- **Files likely affected**: 
  - `pom.xml`
  - `src/main/java/com/example/demo/LocalAiApplication.java`
- **Acceptance Criteria**:
  - The application compiles cleanly with Maven and boots up without throwing auto-configuration errors (e.g. data source or vector store bean errors).
- **Tests required**: 
  - `LocalAiApplicationTests` checking context loads successfully.
- **Notes**: Database configurations are currently excluded in `LocalAiApplication.java` annotation processors to allow isolated testing.

### TS-003
- **Title**: Integrate Apache Tika & Config Bean
- **Phase**: Phase 1
- **Status**: Backlog
- **Description**: Add and configure the Apache Tika parser bean in the Spring context under the `config` package to make it available for injects.
- **Files likely affected**:
  - `src/main/java/com/example/demo/config/TikaConfig.java`
- **Acceptance Criteria**:
  - Tika config bean compiles and is injectable into any Service.
- **Tests required**:
  - Tika injection smoke test.
- **Notes**: Uses Tika standard packages.

### TS-004
- **Title**: Create Document Ingestion Upload Endpoint
- **Phase**: Phase 1
- **Status**: Backlog
- **Description**: Create a controller endpoint to accept multipart file uploads (PDF and DOCX). Add basic file checking (non-empty, file extension).
- **Files likely affected**:
  - `src/main/java/com/example/demo/controller/DocumentController.java`
  - `src/main/java/com/example/demo/dto/UploadResponseDto.java`
- **Acceptance Criteria**:
  - POSTing a PDF to `/api/documents/upload` returns a HTTP 202 Accepted status with metadata.
- **Tests required**:
  - Controller integration tests utilizing `MockMvc` and `MockMultipartFile`.
- **Notes**: Secure endpoint constraints will be layered on in Phase 8.

### TS-005
- **Title**: Create Text Extraction Service
- **Phase**: Phase 1
- **Status**: Backlog
- **Description**: Implement a service using Apache Tika to parse uploaded document input streams and return plain text.
- **Files likely affected**:
  - `src/main/java/com/example/demo/document/parser/TikaParserService.java`
- **Acceptance Criteria**:
  - Feeding a sample PDF to `TikaParserService.parse()` returns accurate, clean plain text matching the file contents.
- **Tests required**:
  - Unit tests utilizing mock CV documents in test resources.
- **Notes**: Handle blank pages or empty PDFs gracefully.

### TS-006
- **Title**: Implement CV Analysis Service
- **Phase**: Phase 2
- **Status**: Backlog
- **Description**: Integrate Spring AI `ChatModel` to send CV raw text alongside structured prompts to local Ollama and return analysis details.
- **Files likely affected**:
  - `src/main/java/com/example/demo/cv/service/CvAnalysisService.java`
- **Acceptance Criteria**:
  - Invoking the analysis service retrieves structured JSON from Ollama.
- **Tests required**:
  - Mock chat client tests confirming the parser structures the parameters correctly.
- **Notes**: Must handle formatting failures.

### TS-007
- **Title**: Create Structured Response DTO
- **Phase**: Phase 2
- **Status**: Backlog
- **Description**: Define the structure of the JSON payload for CV Analysis results including ATS compatibility and keyword matches.
- **Files likely affected**:
  - `src/main/java/com/example/demo/cv/dto/CvAnalysisResponseDto.java`
- **Acceptance Criteria**:
  - Class compiles and handles standard Jackson serialization cleanly.
- **Tests required**:
  - Serialization / deserialization tests.
- **Notes**: Fields include: `atsScore`, `strengths`, `weaknesses`, `recommendations`.

### TS-008
- **Title**: Add Factual Validation for AI Responses
- **Phase**: Phase 7
- **Status**: Backlog
- **Description**: Implement post-processing factual check logic that audits the AI's suggestions to prevent hallucinated accomplishments.
- **Files likely affected**:
  - `src/main/java/com/example/demo/ai/validation/FactualIntegrityService.java`
- **Acceptance Criteria**:
  - Rewrites containing skills not listed in the original text are blocked.
- **Tests required**:
  - Validation tests injecting fake data and verifying rejection.
- **Notes**: Crucial step for trust.

### TS-009
- **Title**: Add Basic Security and Upload File Limits
- **Phase**: Phase 8
- **Status**: Backlog
- **Description**: Implement file size constraints (max 5MB) and mime-type verification (PDF, DOCX only) in the Spring Security filter context.
- **Files likely affected**:
  - `src/main/java/com/example/demo/security/SecurityConfig.java`
- **Acceptance Criteria**:
  - Attempting to upload a 10MB file or an HTML file returns a clear 400 Bad Request error.
- **Tests required**:
  - Security configuration integration tests.
- **Notes**: Prevent denial of service or execution attacks.

---

## Future Phase Backlog (Awaiting Phase Activation)

### TS-010
- **Title**: Configure PGVector & RAG Pipelines (Phase 4)
- **Status**: Backlog
- **Description**: Setup segment chunking and embed them into PGVector database using Ollama embeddings.

### TS-011
- **Title**: Implement Web Product Endpoint Formatting (Phase 9)
- **Status**: Backlog
- **Description**: Structure endpoints to facilitate standard browser integrations and structured error payloads.

### TS-012
- **Title**: Decouple Environment Configuration for Production Deployment (Phase 12)
- **Status**: Backlog
- **Description**: Establish modular system properties and secrets injection workflows for secure hosting profiles.

---

## Future Ideas (Out of Active Scope)
* **Social Network Pathway Mapper**: Map contacts from LinkedIn exports (Phase 13 boundary).
* **AI Binary Ingestion Threat Scanner**: Advanced virus scanning of PDFs using localized LLM patterns (Phase 13 boundary).
