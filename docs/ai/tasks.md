# AI Workstation: Active Task Board

This task board acts as the active work tracker for the **local-ai** project.

## Board Status Summary
- **Current Active Phase**: Phase 20 (AI Layout Unscrambler / Structuring Engine) - Completed
- **Active Sprint Target**: Implement the LLM-based layout parsing and restructuring pass for document ingestion. This will clean up the raw OCR layout text, preserve factual integrity, and structure chunks before vector store embedding, solving column layout fragmentation. [Sprint fully completed and verified].

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
- **Status**: Done
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
- **Status**: Done
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
- **Status**: Done
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
- **Status**: Done
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
- **Status**: Done
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
- **Status**: Done
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
- **Status**: Done
- **Description**: Define the structure of the JSON payload for CV Analysis results including ATS compatibility and keyword matches.
- **Files likely affected**:
  - `src/main/java/com/example/demo/dto/CvAnalysisResponse.java`
- **Acceptance Criteria**:
  - Class compiles and handles standard Jackson serialization cleanly.
- **Tests required**:
  - Serialization / deserialization tests.
- **Notes**: Fields include: `atsScore`, `strengths`, `weaknesses`, `recommendations`.

### TS-008
- **Title**: Add Factual Validation for AI Responses
- **Phase**: Phase 7
- **Status**: Done
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
- **Status**: Done
- **Description**: Implement file size constraints (max 5MB) and mime-type verification (PDF, DOCX only) in the Spring Security filter context.
- **Files likely affected**:
  - `src/main/java/com/example/demo/security/SecurityConfig.java`
- **Acceptance Criteria**:
  - Attempting to upload a 10MB file or an HTML file returns a clear 400 Bad Request error.
- **Tests required**:
  - Security configuration integration tests.
- **Notes**: Prevent denial of service or execution attacks.

### TS-013
- **Title**: Setup Database Dependencies and Configurations
- **Phase**: Phase 3
- **Status**: Done
- **Description**: Add H2 test database dependencies, configure PostgreSQL database property profiles, and remove auto-configuration exclusions.
- **Files likely affected**:
  - `pom.xml`
  - `src/main/java/com/example/demo/LocalAiApplication.java`
  - `src/main/resources/application.properties`
  - `src/test/resources/application-test.properties`
- **Acceptance Criteria**:
  - H2 database operates in tests, PostgreSQL is configured for local profiles, and application compiles cleanly without auto-configuration errors.
- **Tests required**:
  - Spring Context load tests.

### TS-014
- **Title**: Define JPA Entities and Enums
- **Phase**: Phase 3
- **Status**: Done
- **Description**: Implement JPA entities for UserProfile, Document, ExtractedText, CvAnalysis, and the DocumentType enum.
- **Files likely affected**:
  - `src/main/java/com/example/demo/model/*`
  - `src/main/java/com/example/demo/cv/model/*`
- **Acceptance Criteria**:
  - Entities map cleanly with correct annotations, relationships, cascades, and schema rules.

### TS-015
- **Title**: Implement JPA Repository Interfaces
- **Phase**: Phase 3
- **Status**: Done
- **Description**: Implement UserProfileRepository, DocumentRepository, ExtractedTextRepository, and CvAnalysisRepository.
- **Files likely affected**:
  - `src/main/java/com/example/demo/repository/*`
  - `src/main/java/com/example/demo/cv/repository/*`
- **Acceptance Criteria**:
  - Standard Spring Data JPA find, save and query operations are available.

### TS-016
- **Title**: Refactor Services and Controllers for Persistence
- **Phase**: Phase 3
- **Status**: Done
- **Description**: Integrate repositories and DocumentService into DocumentController and CvAnalysisService to persist uploads and analysis results in the database.
- **Files likely affected**:
  - `src/main/java/com/example/demo/controller/DocumentController.java`
  - `src/main/java/com/example/demo/cv/service/CvAnalysisService.java`
  - `src/main/java/com/example/demo/document/service/DocumentService.java`
- **Acceptance Criteria**:
  - Multipart upload and CV analysis endpoints extract, analyze, and save document and analysis details directly into the database.

### TS-010
- **Title**: Configure PGVector & RAG Pipelines
- **Phase**: Phase 4
- **Status**: Done
- **Description**: Setup segment chunking and embed them into PGVector database using Ollama embeddings.
- **Files likely affected**:
  - `src/main/java/com/example/demo/LocalAiApplication.java`
  - `src/main/resources/application.properties`
  - `src/main/java/com/example/demo/config/VectorStoreTestConfig.java`
  - `src/main/java/com/example/demo/ai/rag/service/DocumentChunkingService.java`
  - `src/main/java/com/example/demo/ai/rag/service/EmbeddingService.java`
  - `src/main/java/com/example/demo/document/service/DocumentService.java`
- **Acceptance Criteria**:
  - The application splits text into paragraphs, indexes them using vector store, permits filtering searches, and deletes them cleanly. All tests execute with zero database/network dependencies.

### TS-017
- **Title**: Setup Job Matching Domain Entities and Repositories
- **Phase**: Phase 5
- **Status**: Done
- **Description**: Add JPA entities for `JobAd` and `JobMatchResult` under `com.example.demo.job.model`, along with their Spring Data JPA repository interfaces.
- **Files likely affected**:
  - `src/main/java/com/example/demo/job/model/JobAd.java`
  - `src/main/java/com/example/demo/job/model/JobMatchResult.java`
  - `src/main/java/com/example/demo/job/repository/JobAdRepository.java`
  - `src/main/java/com/example/demo/job/repository/JobMatchResultRepository.java`
- **Acceptance Criteria**:
  - Entities map cleanly with Hibernate schema generation without compile or startup errors.

### TS-018
- **Title**: Create Job Matching Prompt & Mapper
- **Phase**: Phase 5
- **Status**: Done
- **Description**: Setup standard system prompt templates for job matching analysis and design parser rules to cleanly parse LLM JSON responses.
- **Files likely affected**:
  - `src/main/resources/prompts/job-matching-system-prompt.txt`
  - `src/main/java/com/example/demo/job/dto/JobMatchAiResponse.java`
  - `src/main/java/com/example/demo/mapper/JobMatchMapper.java`
- **Acceptance Criteria**:
  - `JobMatchMapper` successfully parses structured JSON even with leading/trailing markdown code block ticks.
- **Tests required**:
  - `JobMatchMapperTest` verifying string parsing rules.

### TS-019
- **Title**: Implement Job Matching Service
- **Phase**: Phase 5
- **Status**: Done
- **Description**: Construct `JobMatchingService` to retrieve relevant CV chunks matching targeted job description, construct Ollama payload, execute localized LLM queries, and save match analysis results.
- **Files likely affected**:
  - `src/main/java/com/example/demo/job/service/JobMatchingService.java`
- **Acceptance Criteria**:
  - Invoking matching service retrieves semantic context from vector database, feeds it to model, saves records, and produces structured outputs.
- **Tests required**:
  - `JobMatchingServiceTest` integration suite with test profile simple vector store mappings.

### TS-020
- **Title**: Create Job Controller REST Endpoint
- **Phase**: Phase 5
- **Status**: Done
- **Description**: Expose `/api/jobs/match` to receive target CV ID and job advertisement text block and delegate analysis to matching service.
- **Files likely affected**:
  - `src/main/java/com/example/demo/job/controller/JobController.java`
  - `src/main/java/com/example/demo/job/dto/JobMatchRequest.java`
  - `src/main/java/com/example/demo/job/dto/JobMatchResponseDto.java`
- **Acceptance Criteria**:
  - POST requests with empty inputs return 400 Bad Request; valid matches return 200 OK with expected JSON.
- **Tests required**:
  - `JobControllerTest` verifying status code mappings and input validation bounds.

### TS-021
- **Title**: Verify Phase 5 with Unit, Slice, and Integration Tests
- **Phase**: Phase 5
- **Status**: Done
- **Description**: Verify that the entire Phase 5 matches specification parameters, executing compiler checks and test runs with zero failures.
- **Files likely affected**:
  - Test suites compiled across Phase 5.
- **Acceptance Criteria**:
  - `mvn clean test` completes successfully with a clean build.

### TS-022
- **Title**: Setup Rewrite Engine Domain Entities and Repositories
- **Phase**: Phase 6
- **Status**: Done
- **Description**: Add JPA entities for `RewriteSuggestion` and style enums under `com.example.demo.rewrite.model`, along with Spring Data JPA repository interfaces under `com.example.demo.rewrite.repository`.
- **Files likely affected**:
  - `src/main/java/com/example/demo/rewrite/model/RewriteStyle.java`
  - `src/main/java/com/example/demo/rewrite/model/RewriteSuggestion.java`
  - `src/main/java/com/example/demo/rewrite/repository/RewriteSuggestionRepository.java`
- **Acceptance Criteria**:
  - Compilation succeeds with standard Hibernate schema generation for `rewrite_suggestions`.

### TS-023
- **Title**: Create Section Rewrite Prompt & Mapper
- **Phase**: Phase 6
- **Status**: Done
- **Description**: Setup standard system prompt templates for section rewrite analysis and design parser rules in `RewriteMapper`.
- **Files likely affected**:
  - `src/main/resources/prompts/section-rewrite-system-prompt.txt`
  - `src/main/java/com/example/demo/rewrite/dto/RewriteAiResponse.java`
  - `src/main/java/com/example/demo/mapper/RewriteMapper.java`
- **Acceptance Criteria**:
  - `RewriteMapper` successfully parses raw LLM JSON responses, cleaning markdown ticks and wrappers.

### TS-024
- **Title**: Implement Factual Validation Guardrails
- **Phase**: Phase 7
- **Status**: Done
- **Description**: Build post-processing factual check logic that audits the AI's suggestions to prevent hallucinated accomplishments (fabricated skills, tools, certifications, or numbers).
- **Files likely affected**:
  - `src/main/java/com/example/demo/ai/validation/FactualIntegrityService.java`
  - `src/main/java/com/example/demo/ai/validation/FactualValidationException.java`
- **Acceptance Criteria**:
  - Validations intercept, reject, and throw exceptions for suggestions containing unrepresented proper nouns or fabricated metrics.

### TS-025
- **Title**: Implement Section Rewrite Service
- **Phase**: Phase 6
- **Status**: Done
- **Description**: Construct `RewriteService` to fetch CV documents, escape XML injection tags, query local Ollama model, validate output against `FactualIntegrityService`, and persist suggestion caches.
- **Files likely affected**:
  - `src/main/java/com/example/demo/rewrite/service/RewriteService.java`
- **Acceptance Criteria**:
  - Service orchestrates complete rewrite flow, saving record and returning response DTOs.

### TS-026
- **Title**: Create Rewrite REST Controller
- **Phase**: Phase 6
- **Status**: Done
- **Description**: Expose `POST /api/rewrite/section` to receive target CV ID, sentence, and keywords, delegate optimization to service, and map validation errors to HTTP 422.
- **Files likely affected**:
  - `src/main/java/com/example/demo/rewrite/controller/RewriteController.java`
  - `src/main/java/com/example/demo/rewrite/dto/RewriteRequest.java`
  - `src/main/java/com/example/demo/rewrite/dto/RewriteResponseDto.java`
- **Acceptance Criteria**:
  - API accepts requests, validating parameters, returning 200 OK for matches and 422 Unprocessable Entity for hallucinations.

### TS-027
- **Title**: Verify Phase 6 & 7 with Unit, Slice, and Integration Tests
- **Phase**: Phase 6/7
- **Status**: Done
- **Description**: Verify the entire Rewrite implementation, compiling cleanly and passing all test suites.
- **Files likely affected**:
  - Test suites across Rewrite package and Validation package.
  - `src/test/java/com/example/demo/mapper/RewriteMapperTest.java`
  - `src/test/java/com/example/demo/ai/validation/FactualIntegrityServiceTest.java`
  - `src/test/java/com/example/demo/rewrite/service/RewriteServiceTest.java`
  - `src/test/java/com/example/demo/rewrite/controller/RewriteControllerTest.java`
- **Acceptance Criteria**:
  - Maven tests build and run completely green with 100% stable execution.

### TS-011
- **Title**: Implement Web Product Endpoint Formatting & Hardening
- **Phase**: Phase 9
- **Status**: Done
- **Description**: Structure endpoints to facilitate standard browser integrations and structured error payloads, centralize exceptions, and configure dynamic CORS allowed origins.
- **Files likely affected**:
  - `src/main/java/com/example/demo/controller/ChatTestController.java`
  - `src/main/java/com/example/demo/controller/DocumentController.java`
  - `src/main/java/com/example/demo/controller/GlobalExceptionHandler.java`
  - `src/main/java/com/example/demo/security/SecurityConfig.java`
  - `src/main/resources/application.properties`
  - `src/test/java/com/example/demo/controller/ChatTestControllerTest.java`
  - `src/test/java/com/example/demo/controller/DocumentControllerTest.java`
  - `src/test/java/com/example/demo/controller/GlobalExceptionHandlerTest.java`
  - `src/test/java/com/example/demo/security/CorsSecurityConfigTest.java`
- **Acceptance Criteria**:
  - GET `/api/chat` returns standard JSON.
  - Exception handler correctly catches and wraps standard system, validation, and parsing exceptions to unified JSON.
  - CORS configurations are property-driven and dynamically restrict credentials when wildcards are active.
  - 100% compile and testing success under Spring Boot 4.0.6.

### TS-012
- **Title**: Decouple Environment Configuration & Containerize for Production
- **Phase**: Phase 12
- **Status**: Done
- **Description**: Establish modular system properties, externalize environment variables, set up base defaults and prod-specific overrides, construct secure multi-stage Docker configurations, compose multi-service compose stacks, and verify configurations with robust environment smoke tests.
- **Files likely affected**:
  - `src/main/resources/application.properties`
  - `src/main/resources/application-prod.properties`
  - `src/test/java/com/example/demo/config/DeploymentProfileSmokeTest.java`
  - `Dockerfile`
  - `docker-compose.yml`
- **Acceptance Criteria**:
  - Properties resolve dynamically from environment variables with safe defaults.
  - Production settings strictly isolate CORS configurations and database validation parameters (`ddl-auto=validate`).
  - Containerization builds cleanly under a multi-stage JVM execution model and runs as a non-privileged `spring` system user.
  - Smoke tests pass and confirm property resolutions.
  - 100% build and test suite success.

### TS-045
- **Title**: Integrate ArchUnit Dependency
- **Phase**: Phase 13
- **Status**: Done
- **Description**: Add `archunit-junit5` (version `1.3.0`) dependency in test scope to Maven POM, facilitating code-level architectural validations.
- **Files likely affected**:
  - `pom.xml`
- **Acceptance Criteria**:
  - ArchUnit library compiles cleanly and is available under the testing classpaths.

### TS-046
- **Title**: Implement Package Boundary & Layering ArchUnit Rules
- **Phase**: Phase 13
- **Status**: Done
- **Description**: Design and execute automated ArchUnit assertions whitelisting core modules, protecting active Career Document slices from future packages (`socialnetwork`, `risk`, `securityanalysis`), and enforcing clean layered architectural rules.
- **Files likely affected**:
  - `src/test/java/com/example/demo/architecture/PackageBoundaryArchitectureTest.java`
- **Acceptance Criteria**:
  - Protects active career slice packages from conceptual future areas.
  - Enforces strict Controller -> Service -> Repository hierarchy rules.
  - Guarantees "Thin Controllers & Rich Services" by preventing direct repository access from controllers.
  - All 4 architectural rules execute and pass cleanly as part of `mvn clean test`.

### TS-049
- **Title**: Scaffold React/Vite Frontend & Core Shell UI Layout
- **Phase**: Phase 14
- **Status**: Done
- **Description**: Scaffold the modern frontend workspace using Vite with React/TypeScript or standard Vanilla CSS variables, implement Dark/Light theme switching controls, and build the premium responsive layout shells.

### TS-050
- **Title**: Build Interactive Document Ingestion & Scan Dashboard
- **Phase**: Phase 15
- **Status**: Done
- **Description**: Design dropzone uploading state panels, build the progress bar spinners, connect multipart API triggers, and draw custom interactive ATS score gauges.

### TS-054
- **Title**: Centralize Design Tokens and Global CSS
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Add CSS variables for the corporate deep blue and teal light-mode palette in `index.css`. Import and configure the local Inter font. Remove references to WebGL backgrounds, glow effects, neon borders, and glassmorphism. Add standard focus rings and accessibility base styles.
- **Files likely affected**:
  - `frontend/src/index.css`
  - `frontend/src/App.css`
- **Acceptance Criteria**:
  - Centralized design system is declared, compiles cleanly, and the base light background and dark text colors are applied to the body.

### TS-055
- **Title**: Redesign Sidebar and Application Layout Shell
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Redesign the sidebar in `SidebarShell.tsx` using the new design system: solid white background, standard border, user-facing navigation labels (CV Review, Job Match, Bullet Optimizer, System Monitor). Move the System Monitor view under a visually separated "System" section. Display a compact active-document context in the sidebar. Apply the teal indicator and light blue-grey background to the active navigation item.
- **Files likely affected**:
  - `frontend/src/components/SidebarShell.tsx`
- **Acceptance Criteria**:
  - Navigation matches design specs, active-document context displays cleanly without the full path, and System Monitor is visually separated.

### TS-056
- **Title**: Redesign CV Review View (Dashboard & Dropzone)
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Redesign `DocDropzone.tsx` and `AnalysisDashboard.tsx` to match the light-mode theme. In the dropzone, replace WebGL animations and glows with a clean gray border and a clear processing stepper. In the dashboard, replace the neon circular gauge with a horizontal progress track and numerical score badge. Rename "Weaknesses" to "Improvement opportunities" and format lists with clean semantic colors (green/amber/red).
- **Files likely affected**:
  - `frontend/src/components/DocDropzone.tsx`
  - `frontend/src/components/AnalysisDashboard.tsx`
- **Acceptance Criteria**:
  - Dropzone uses a clean border layout with step-based indicators, circular dials are removed, and ATS score is rendered as a clean horizontal progress bar.

### TS-057
- **Title**: Redesign Job Match View (Matcher)
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Redesign the match workspace layout in `App.tsx` (matcher tab). Add the empty state for when no document is active. Implement a clean two-column comparison workspace (active CV summary on the left, job description textarea on the right). Rename primary action to "Analyse job match". Abstract Tika, PGVector, and Ollama technical details from the match results page, displaying user-understandable sections (Alignment score, Aligned vs Missing requirements, Keyword opportunities).
- **Files likely affected**:
  - `frontend/src/App.tsx`
- **Acceptance Criteria**:
  - Matcher workspace uses high-contrast cards, inputs are clearly labeled, and results abstract developer jargon.

### TS-058
- **Title**: Redesign Bullet Optimizer View (Rewriter)
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Redesign the bullet optimization workspace in `App.tsx` (rewriter tab). Create a document-first layout with clickable extracted CV sentences on the left and stylistic controls/results on the right. Rename "Metrics-driven" style to "Results-focused". Format style selections as clear controls with explanation texts. Display suggestions clearly side-by-side or below the original sentence with copy/apply action controls.
- **Files likely affected**:
  - `frontend/src/App.tsx`
- **Acceptance Criteria**:
  - Original sentence remains visible throughout, selected state uses teal indicators and light-blue highlights, results are visually distinct, and a factual check status badge is rendered.

### TS-059
- **Title**: Redesign System Monitor View (Admin)
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Redesign the Telemetry dashboard in `App.tsx` (admin tab). Render dense, professional tables, metric cards, and charts using the corporate blue and gray light palette.
- **Files likely affected**:
  - `frontend/src/App.tsx`
- **Acceptance Criteria**:
  - No terminal/neon styles remain, telemetry is clean and matches the design tokens, and developer terminology is clearly isolated.

### TS-060
- **Title**: Validate Accessibility, Responsiveness, and Clean Codebase
- **Phase**: Phase 19
- **Status**: Done
- **Description**: Verify the redesign against WCAG 2.2 AA standards (color contrast, keyboard focus indicators, screen readers) and run responsive tests across mobile, tablet, and desktop widths. Remove legacy assets (`Silk.tsx` and glow utility imports) and confirm there are no compile warnings.
- **Files likely affected**:
  - `frontend/src/components/animations/Silk.tsx`
- **Acceptance Criteria**:
  - Contrast checks pass, all actions are keyboard-navigable, layouts stack correctly on mobile, and legacy WebGL/animation files are retired.

### TS-051
- **Title**: Build Dual-Column Job Match Gaps UI
- **Phase**: Phase 16
- **Status**: Done
- **Description**: Design side-by-side matching screens, connect paste/submit description hooks, render missing keywords tags, and compile gap comparison layouts.
- **Files likely affected**:
  - `frontend/src/App.tsx`
- **Acceptance Criteria**:
  - Compares active document context against job descriptions, exposes matching scores and recommendations, and features a clean comparison workspace.

### TS-052
- **Title**: Build Factual Sentence Rewriting Interface
- **Phase**: Phase 17
- **Status**: Done
- **Description**: Render interactive sentence selectors, configure style modes (Action-oriented, Concise, Metrics-driven), and map proper noun alert flags.
- **Files likely affected**:
  - `frontend/src/App.tsx`
- **Acceptance Criteria**:
  - Splits parsed CV content into clickable sentences, provides style results, maps copy triggers, and displays blocked factual error messages.

### TS-053
- **Title**: Build Telemetry Monitor visual analytics dashboard
- **Phase**: Phase 18
- **Status**: Done
- **Description**: Construct high-fidelity charts detailing sub-agent processing timelines, P95 latency profiles, and token footprints.
- **Files likely affected**:
  - `frontend/src/App.tsx`
- **Acceptance Criteria**:
  - Renders telemetry timelines, model metadata summaries, and processing metrics in clean, light-mode dashboards.

### TS-061
- **Title**: Create CV Ingestion Pre-Processor Service
- **Phase**: Phase 20
- **Status**: Done
- **Description**: Implement a service `CvStructuringService` that uses a system prompt template to structure and clean up raw layout-scrambled CV text (retrieved from Tika) into standardized Markdown format with sections (Summary, Experience, Education, Projects, Skills, Languages, Contacts) before it is chunked and stored.
- **Files likely affected**:
  - `src/main/resources/prompts/cv-structuring-prompt.txt`
  - `src/main/java/com/example/demo/ai/parser/CvStructuringService.java`
- **Acceptance Criteria**:
  - Feeding a jumbled OCR string (with column-merged layout newlines) to the structuring service returns clean, restructured Markdown sections with preserved dates, names, and metrics.
- **Tests required**:
  - Unit tests with mock LLM responses validating that formatting errors are resolved and text structure is clean.

### TS-062
- **Title**: Integrate Pre-Processor into Ingestion Pipeline
- **Phase**: Phase 20
- **Status**: Done
- **Description**: Update `DocumentService.java` to route raw text through `CvStructuringService` before saving `ExtractedText` and indexing it in the vector store.
- **Files likely affected**:
  - `src/main/java/com/example/demo/document/service/DocumentService.java`
- **Acceptance Criteria**:
  - Uploading a dual-column CV cleanly persists structured text, resolving mid-sentence splits, and indexes structured vector chunks.
- **Tests required**:
  - Integration tests simulating a PDF upload and verifying that `rawContent` of `ExtractedText` contains the structured markdown text.

### TS-063
- **Title**: Add Factual Safeguards for CV Restructuring
- **Phase**: Phase 20
- **Status**: Done
- **Description**: Integrate factual validation checks to ensure the LLM-restructured CV text does not omit or fabricate key dates, companies, or programming languages from the original raw OCR text, falling back to raw text if validation fails.
- **Files likely affected**:
  - `src/main/java/com/example/demo/document/service/DocumentService.java`
- **Acceptance Criteria**:
  - A CV structuring output that fabricates or drops experience is rejected by the pre-processor, falling back to storing the raw Tika content safely rather than failing the upload.
- **Tests required**:
  - Unit tests verifying the fallback mechanism when restructuring fails validation rules.

### TS-064
- **Title**: Implement Hybrid Vector & Keyword Search
- **Phase**: Phase 20 (Category A)
- **Status**: Done
- **Description**: Add a hybrid search capability that merges dense vector search (PGVector) with sparse keyword queries (PostgreSQL Full-Text Search) to ensure exact technology matches alongside semantic leadership/experience matching.
- **Files likely affected**:
  - `src/main/java/com/example/demo/ai/rag/service/EmbeddingService.java`
  - `src/main/java/com/example/demo/job/service/JobMatchingService.java`
- **Acceptance Criteria**:
  - Running queries for exact keywords (e.g. "Kotlin" or "RSA") returns documents containing those exact keywords even if they are semantically distant, while general queries (e.g., "internship developer") return semantic matches.
- **Tests required**:
  - Unit tests verifying the merging logic and ranking priorities.

### TS-065
- **Title**: Implement Query Expansion using Local LLM
- **Phase**: Phase 20 (Category B)
- **Status**: Done
- **Description**: Implement a query pre-processing pass that expands the user's search queries with synonyms and alternative terms (e.g., matching "frontend" with "React, JavaScript, CSS") using a quick pass to `gemma2:2b`.
- **Files likely affected**:
  - `src/main/java/com/example/demo/ai/rag/service/EmbeddingService.java`
- **Acceptance Criteria**:
  - Searching for "Frontend developer" automatically matches documents referencing "React" or "CSS" even if "frontend" is not explicitly mentioned.
- **Tests required**:
  - Unit tests validating query expansion formatting.

### TS-066
- **Title**: Implement Hierarchical Metadata Filters
- **Phase**: Phase 20 (Category B)
- **Status**: Done
- **Description**: Add hierarchical RAG filters using document metadata, allowing users to filter searches by logical document boundaries or parent sections before similarity scores are computed.
- **Files likely affected**:
  - `src/main/java/com/example/demo/ai/rag/service/EmbeddingService.java`
- **Acceptance Criteria**:
  - Queries can be filtered dynamically using metadata properties in the similarity request.
- **Tests required**:
  - Unit tests validating metadata filter injection.

---

## Future Phase Backlog (Awaiting Phase Activation)
None

---

## Future Ideas (Out of Active Scope)
* **Social Network Pathway Mapper**: Map contacts from LinkedIn exports (Phase 13 boundary).
* **AI Binary Ingestion Threat Scanner**: Advanced virus scanning of PDFs using localized LLM patterns (Phase 13 boundary).
