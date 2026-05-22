# AI Workstation: Roadmap & Phased Execution

This roadmap structures the development of the **local-ai** application. It establishes concrete milestones, logic boundaries, testing targets, and risk mitigations for each phase. Our first MVP focuses entirely on the **Career Document Analysis** module.

---

## Phase 0: Foundation and Project Context
- **Goal**: Establish a stable application baseline, resolve initial configurations, and verify the local connection to Ollama.
- **Logic to Implement**: 
  - Define custom connection properties for Spring AI and Ollama in `application.properties`.
  - Create a basic chat tester controller to send a message to local Ollama and return text.
- **Packages Affected**: `config`, `controller`
- **Acceptance Criteria**:
  - GET `/api/chat?message=Hello` successfully queries the local Ollama instance (`gemma4:2eb` model) and returns the text response.
- **Tests to Add**:
  - Integration test verifying the `/api/chat` endpoint is responsive (mocking the `ChatModel` for normal test runs, using a specific profile for live integration tests).
- **Risks**: Local Ollama instance is not running or model is not downloaded. Requires clear developer setup verification.

---

## Phase 1: Document Upload and Text Extraction
- **Goal**: Accept document uploads (PDF, DOCX) and cleanly extract raw text and metadata.
- **Logic to Implement**:
  - Configure **Apache Tika** bean to handle parsed documents.
  - Implement a `DocumentService` utilizing Tika's parser to extract plain text from incoming files.
  - Create an ingestion REST endpoint that accepts `MultipartFile` and returns raw text.
- **Packages Affected**: `document/parser`, `document/storage`, `controller`, `dto`
- **Acceptance Criteria**:
  - POST `/api/documents/upload` accepts a multi-part file (PDF) and returns a JSON payload containing the extracted clean text.
- **Tests to Add**:
  - Unit tests for `TikaParserService` with mock PDF and DOCX files.
  - Controller test verifying size limits and payload structure.
- **Risks**: Extraction fails on highly formatted tables, images, or scanned PDFs. Must log parsing warnings instead of returning 500 errors.

---

## Phase 2: Basic CV Analysis with Structured Output
- **Goal**: Send extracted text to local AI and receive a structured, parsed JSON response detailing CV strengths, weaknesses, and ATS metrics.
- **Logic to Implement**:
  - Define system and user prompt templates for CV analysis using Spring AI's Prompt Template resources.
  - Create a structured `CvAnalysisResponse` DTO mapping ATS Score, Strengths List, Weaknesses List, and Suggested Improvements.
  - Use Spring AI's `MapOutputConverter` or structured output classes to enforce JSON compliance.
- **Packages Affected**: `ai/prompt`, `cv/service`, `cv/dto`, `analysis/dto`
- **Acceptance Criteria**:
  - POST `/api/cv/analyze` accepts extracted text (or raw document text) and returns a validated JSON object conforming to the structured schema.
- **Tests to Add**:
  - Unit test for parsing logic using mock LLM JSON responses.
  - Assert that negative/empty inputs are handled safely.
- **Risks**: The local model (`gemma4:2eb`) fails to follow JSON format instructions consistently. Requires robust fallback parser logic.

---

## Phase 3: Persistence and Domain Model
- **Goal**: Define the JPA entities and persistence layers to persist documents, extracted text, and analysis history.
- **Logic to Implement**:
  - Enable PostgreSQL/Data JPA auto-configuration.
  - Create database entities: `UserProfile`, `Document`, `ExtractedText`, `CvAnalysis`.
  - Establish relationship mapping: A User Profile can have multiple Documents, each having one ExtractedText and multiple CvAnalysis reports.
- **Packages Affected**: `model`, `repository`, `cv/model`, `cv/repository`
- **Acceptance Criteria**:
  - Saving a document persists its metadata and links it to an extracted text record and analysis run in the database.
- **Tests to Add**:
  - DataJPA repository tests verifying cascade types, database constraints, and custom query methods.
- **Risks**: Large PDF raw text files could exceed DB column size limits. Must use `@Lob` or PostgreSQL text column definitions.

---

## Phase 4: Embeddings and PGVector RAG
- **Goal**: Implement segment chunking, generate vector embeddings using Ollama, and index chunks into PGVector for semantic retrieval.
- **Logic to Implement**:
  - Implement a recursive or window-based text chunking service.
  - Configure the Spring AI `PgVectorStore` bean.
  - Implement an `EmbeddingService` to store and semantically search through CV document sections.
- **Packages Affected**: `ai/rag`
- **Acceptance Criteria**:
  - The application splits a 5-page CV into distinct logical paragraphs, indexes them, and allows semantic search queries (e.g. "Software developer internship experience") to return the most relevant text chunks.
- **Tests to Add**:
  - Integration tests for `VectorStore` operations (saving, deleting, similarity search).
- **Risks**: High latency during embedding generation on local machines without hardware acceleration.

---

## Phase 5: Job Ad Matching and Keyword Optimization
- **Goal**: Perform semantic analysis matching CV sections against specific job advertisement descriptions.
- **Logic to Implement**:
  - Build a matching service that retrieves CV semantic chunks relevant to a target job ad description.
  - Prompt local AI to evaluate gaps, missing keywords, alignment score, and actionable optimization strategies.
- **Packages Affected**: `job/service`, `job/dto`, `analysis/service`
- **Acceptance Criteria**:
  - POST `/api/jobs/match` accepts a CV document ID and a Job Ad text block, returning missing keywords, alignment percentage, and optimization tips.
- **Tests to Add**:
  - Unit tests validating that the matching prompt incorporates both the CV chunks and the job ad text under strict delimiters.
- **Risks**: Extreme context window bloat if both the full CV chunks and long job ads are sent together. Requires strict token limits on chunks.

---

## Phase 6: Rewrite Engine
- **Goal**: Implement an interactive engine to optimize selected CV bullet points or sections based on job keywords.
- **Logic to Implement**:
  - Build a rewrite service that accepts a CV segment, user prompt, and a list of target keywords.
  - Prompt the local AI to provide three stylistic options (action-oriented, metrics-driven, concise) while strictly forbidding factual fabrication.
- **Packages Affected**: `rewrite/service`
- **Acceptance Criteria**:
  - POST `/api/rewrite/section` returns three highly optimized variations of the target text that preserve all factual details.
- **Tests to Add**:
  - Tests ensuring that output suggestions preserve original dates, numbers, and companies.
- **Risks**: Model ignores restrictions and fabricates extra work context. Mitigated through validation guardrails in Phase 7.

---

## Phase 7: Validation and Guardrails
- **Goal**: Ensure absolute factual integrity of AI-generated rewrites and structured analyses.
- **Logic to Implement**:
  - Build post-processing checkers (`ai/validation/FactualIntegrityChecker`) to compare the raw CV text against the AI's suggested rewrites.
  - Verify that the suggested rewrite does not contain nouns (skills, companies, tools) or numbers (dates, percentages) not present in either the original text or explicitly supplied user context.
- **Packages Affected**: `ai/validation`
- **Acceptance Criteria**:
  - A rewrite containing a fabricated certification (e.g., "AWS Certified") when the original CV has no AWS mention is caught, rejected, and logged.
- **Tests to Add**:
  - Rigorous negative test suite injecting hallucinations and verifying that the guardrails flag and block them.
- **Risks**: Overly sensitive string matching blocks valid linguistic rewrites. Guardrails must distinguish between synonyms and entirely new facts.

---

## Phase 8: Security and User Data Protection
- **Goal**: Secure file upload handling, establish Spring Security boundaries, and enforce strict PII logging exclusions.
- **Logic to Implement**:
  - Restrict file uploads to authentic PDF and DOCX magic numbers (not just file extensions).
  - Enforce a 5MB size limit on incoming files.
  - Set up logging filters to scrub and exclude raw CV text or contact information from application console outputs.
- **Packages Affected**: `security`, `config`
- **Acceptance Criteria**:
  - Uploading a renamed executable file is immediately rejected.
  - Secure logs show transaction IDs without exposing CV names, addresses, or phone numbers.
- **Tests to Add**:
  - Security integration tests verifying that malformed files are rejected.
  - Log verification checks confirming PII scrubbing works.
- **Risks**: Vulnerabilities in third-party parsers like Apache Tika. Mitigated by keeping parsers updated and running under restricted system privileges.

---

## Phase 9: Web Product Layer
- **Goal**: Structure clean REST endpoints to connect with a standard browser client or frontend interface.
- **Logic to Implement**:
  - Expose API endpoints using consistent JSON response structures.
  - Enable unified exception handlers returning standard API error payloads.
  - Configure CORS allowed origins dynamically based on active profile.
- **Packages Affected**: `web`, `cv/controller`, `dto`
- **Acceptance Criteria**:
  - Standardized JSON responses for all REST operations, allowing a decoupled React/Next.js client to render progress bars, error toast notifications, and forms.
- **Tests to Add**:
  - MockMvc controller contract tests ensuring response structures never change unexpectedly.
- **Risks**: Direct cross-origin vulnerabilities if CORS is misconfigured.

---

## Phase 10: Agent & Workflow Orchestration
- **Goal**: Split CV processing into a sequence of specialized AI workflows (agents) to achieve deeper results.
- **Logic to Implement**:
  - Create orchestrator services that sequentially invoke specialized agents:
    1. **ATS Agent**: Scans formatting and header structures.
    2. **Recruiter Agent**: Reviews readability and impact.
    3. **Keyword Agent**: Extracts and checks keyword density.
    4. **Validation Agent**: Audits factual compliance of suggestions.
- **Packages Affected**: `ai/workflow`
- **Acceptance Criteria**:
  - Querying analysis triggers a multi-step workflow where output from each step is fed into a consolidated structured review.
- **Tests to Add**:
  - Workflow orchestrator tests verifying state machine transitions and execution timeouts.
- **Risks**: High latency when running multiple local AI inferences sequentially. Workflow must execute asynchronously.

---

## Phase 11: Hardening and Evaluation
- **Goal**: Maximize system stability, evaluate LLM prompt performance metrics, and build testing fixtures.
- **Logic to Implement**:
  - Establish a set of local CV evaluation fixtures.
  - Implement system metrics tracking response latencies, model call success rates, and token counts.
- **Packages Affected**: `service`, `config`
- **Acceptance Criteria**:
  - Comprehensive dashboard-ready logs and test runs demonstrating stability and scoring metrics over at least 50 test runs.
- **Tests to Add**:
  - Large-scale validation and load simulation tests.
- **Risks**: Local hardware performance variations. Mitigated by setting execution timeout limits.

---

## Phase 12: Deployment Readiness
- **Goal**: Decouple deployment environments and package the application for public cloud deployment.
- **Logic to Implement**:
  - Standardize Spring Profiles (`local`, `test`, `prod`).
  - Configure DB storage connections and credentials using standard system environment variables (to run under Docker Compose or Kubernetes).
  - Draft deployment instructions separating developer Ollama from server inference containers.
- **Packages Affected**: `config`, root directory (Dockerfiles / compose files)
- **Acceptance Criteria**:
  - The application boots and functions in `prod` mode, pointing to a secure database and a remote host running Ollama, without compiling local mock shortcuts.
- **Tests to Add**:
  - Setup and environment smoke tests.
- **Risks**: Misconfigured environment variables lead to connection timeouts in production.

---

## Phase 13: Future Module Readiness
- **Goal**: Ensure the codebase remains modular, clean, and extensible to incorporate future scopes without carrying current implementation overhead.
- **Logic to Implement**:
  - Verify package boundaries. Ensure that `cv`, `job`, and `rewrite` packages maintain zero dependencies on conceptual future spaces like `socialnetwork` or `risk`.
- **Packages Affected**: Root architectural review
- **Acceptance Criteria**:
  - Core Career Document modules compile and run as isolated components, meaning future modules can be hot-swapped or added cleanly as separate vertical slices.
- **Tests to Add**:
  - ArchUnit tests verifying package boundary violations.
- **Risks**: Architectural drift. Mitigated by maintaining strict ADR rules.
