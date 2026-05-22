# AI Workstation: Agent Skills Registry

This registry defines the specialized capabilities (skills) that AI assistants (including Gemini / Antigravity CLI) are equipped to perform on the **local-ai** codebase. It outlines the purpose, context, pre-requisites, and validation requirements for each skill.

---

## Skill Directory & Specifications

### 1. Spring Boot Implementation
- **Purpose**: Implement high-quality Spring Boot Java 21 classes matching architecture designs.
- **When to Use**: Writing services, repository operations, entity schemas, config beans, or security filters.
- **Required Reading**: [architecture.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/architecture.md), [best-practices.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/best-practices.md), [styleguide.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/styleguide.md).
- **Expected Output**: Clean, compilation-ready Java source code files utilizing modern patterns.
- **Validation**: Strict compile checks (`mvn clean compile`) and no field injection.

### 2. REST API & Web Design
- **Purpose**: Design clean, standardized REST APIs compliant with browser requirements.
- **When to Use**: Creating or modifying HTTP controllers, mapping paths, and defining request/response structures.
- **Required Reading**: [api-contracts.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/api-contracts.md), [validation.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/validation.md).
- **Expected Output**: REST controllers returning validated DTO payloads and utilizing consistent HTTP status codes.
- **Validation**: Verify that request bodies are bound using `@Valid` and response fields use standard JSON formats.

### 3. Document Parsing (Apache Tika)
- **Purpose**: Safely ingest binary files (PDF, DOCX) and cleanly extract raw text.
- **When to Use**: Setting up file upload services, configuring parsing logic, or adjusting text extraction rules.
- **Required Reading**: [security.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/security.md), [architecture.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/architecture.md).
- **Expected Output**: Parsers returning clean plain text, with file magic bytes fully validated.
- **Validation**: Assert extraction efficiency on dummy files in the test suite.

### 4. Prompt Engineering & Design
- **Purpose**: Formulate safe, delimiters-separated, and instruction-defended LLM prompt resources.
- **When to Use**: Writing or updating model instruction sets for CV analysis, job matching, or section rewrites.
- **Required Reading**: [prompts.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/prompts.md), [validation.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/validation.md), [security.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/security.md).
- **Expected Output**: Externalized prompt templates with strict instruction delimiters and neutralizations.
- **Validation**: Audit prompt structures against injection vectors.

### 5. Structured AI Output Design
- **Purpose**: Direct local LLMs to output pure, parseable, and schema-compliant JSON payloads.
- **When to Use**: Structuring model instructions or writing response deserialization methods.
- **Required Reading**: [validation.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/validation.md).
- **Expected Output**: Dynamic schema templates mapping directly to Java validation records/DTOs.
- **Validation**: Assert parsing success across sample outputs in the test layer.

### 6. Ollama & Spring AI Integration
- **Purpose**: Configure connections and process conversations using the Spring AI Chat Client interface.
- **When to Use**: Writing services that interact with local `gemma4:2eb` runtime models.
- **Required Reading**: [context.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/context.md), [ADR-0001-local-first-ai.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/decisions/ADR-0001-local-first-ai.md).
- **Expected Output**: Clean integrations utilising standard `ChatModel` interfaces.
- **Validation**: Verification via Mock-based unit assertions (no live local network calls in CI).

### 7. PGVector RAG Implementation
- **Purpose**: Implement semantic retrieval pipelines, chunking raw inputs and generating vector stores.
- **When to Use**: Implementing semantic search operations, job ad matching, and resume index searches.
- **Required Reading**: [architecture.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/architecture.md), [domain-model.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/domain-model.md).
- **Expected Output**: Chunking services, embedding calculators, and search integrations utilizing Spring AI's `VectorStore`.
- **Validation**: Perform RAG semantic recall checks under custom integration tests.

### 8. Security Review & Sanitization
- **Purpose**: Identify prompt injection vectors, sanitize files, and enforce user access boundaries.
- **When to Use**: Modifying file ingestion methods, auditing logging files, or designing CORS rules.
- **Required Reading**: [security.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/security.md), [validation.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/validation.md).
- **Expected Output**: Code components scrubbed of security threats and hardened logging filters.
- **Validation**: Assert rejection bounds in security test fixtures.

### 9. Test Suite Generation
- **Purpose**: Formulate rapid, mock-based unit tests and comprehensive MockMvc endpoint assertions.
- **When to Use**: Accompanying any new feature implementation slice or fixing bugs.
- **Required Reading**: [testing.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/testing.md).
- **Expected Output**: Clean JUnit 5 test classes utilizing Mockito frameworks.
- **Validation**: Verify that standard tests run within milliseconds and do not require live model runtimes.

### 10. Refactoring & Code Cleaning
- **Purpose**: Simplify execution paths, optimize algorithms, and clean class dependencies.
- **When to Use**: Consolidating code blocks after a phase milestone is reached.
- **Required Reading**: [styleguide.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/styleguide.md), [best-practices.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/best-practices.md).
- **Expected Output**: Highly optimized, concise, and cleaner classes with no behavioral degradation.
- **Validation**: Confirm all unit tests pass green.

### 11. Documentation Governance
- **Purpose**: Keep context documents, roadmap timelines, task registries, and ADR lists perfectly aligned.
- **When to Use**: Creating new modules, modifying API endpoints, or starting new roadmap phases.
- **Required Reading**: [workflow.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/workflow.md).
- **Expected Output**: Updated, consistent, and structured Markdown files inside `/docs/ai/`.
- **Validation**: Validate that file paths and package references are accurate.

### 12. Deployment Setup & Environment Mapping
- **Purpose**: Decouple configurations, manage profile dependencies, and secure production credentials.
- **When to Use**: Preparing code for hosting, setting up profiles, or writing compose specifications.
- **Required Reading**: [deployment.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/deployment.md).
- **Expected Output**: Standardized properties, secure environment wrappers, and deployment notes.
- **Validation**: Verify standard properties boot in isolated profiles.

---

## Scope Rules
- **Active Skills Restriction**: All active coding operations must be restricted to the Career Document Analysis skills.
- **Skills for Social Network Analysis and AI Security Scanning**: Modularity skills for analyzing networks, scanning malicious processes, or performing advanced threat analyses are **strictly deferred**. They must not be utilized or added until explicitly requested by the user.
