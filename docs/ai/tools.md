# AI Workstation: Tool Registry

This document lists the core developer and runtime tools configured for the **local-ai** workspace. It provides instructions on when to use each tool, common commands, and critical runtime notes.

---

## 1. Primary Developer & Compile Tools

### Maven
- **Purpose**: Dependency management, compiler, build lifecycles, and test runner.
- **When to Use**: Running compilation checks, executing standard test loops, and building packaging jars.
- **Commands**:
  - `mvn clean compile` (Verify zero compile warnings)
  - `mvn test` (Execute the fast standard test suite)
  - `mvn test -Pintegration-test` (Execute local Ollama integration assertions)
- **Cautions**: Keep properties in sync with `pom.xml`. Do not add third-party maven repositories without user approval.

### Java 21 & Spring Boot
- **Purpose**: Base compiler engine and framework container.
- **When to Use**: Core coding operations and application configurations.
- **Notes**: Leverage modern language features like records, pattern matching, and enhanced switch blocks.

### Git
- **Purpose**: Version control and code traceability.
- **When to Use**: Commit work, review file diffs, and isolate task branches.
- **Notes**: Commit messages must refer to the active task ID (e.g. `feat(cv): TS-005 add tika parser`).

---

## 2. Ingestion & Storage Parsers

### Apache Tika
- **Purpose**: Binary document content and metadata extraction parser.
- **When to Use**: Processing incoming multipart file streams during ingestion.
- **Notes**: Decouple the Tika service from controllers. Set strict file size limits (5MB) prior to feeding streams.

---

## 3. Core Database & Vector Engines

### PostgreSQL & PGVector
- **Purpose**: Relational schema persistence and multi-dimensional vector store indexing.
- **When to Use**: Persisting user metadata, saving document parsed states, and retrieving semantic context (RAG).
- **Notes**: Ensure the `pgvector` extension is enabled in target PostgreSQL installations. Decouple connection logic using environment variables.

---

## 4. Runtime & Assistant AI Tools

### Ollama (Runtime Local AI)
- **Purpose**: Performs local, privacy-respecting LLM inference and embeddings generation.
- **Model**: `gemma4:2eb`
- **Connection**: Configured via Spring AI (`http://localhost:11434` in local dev).
- **Critical Note**: Ollama is the core runtime AI for the application itself. It runs in the backend context. **End-users do not need to install Ollama**. In future deployments, Ollama runs on dedicated server-side inference hardware.

### Gemini / Antigravity CLI (Development Assistant)
- **Purpose**: Code generation, architecture planning, and test composition.
- **When to Use**: Pair-programming, debugging, and task status board updates.
- **Note**: The developer assistant is used solely during engineering phases and is completely decoupled from the runtime application logic. It must never be referred to or called by the Spring Boot code.

---

## 5. REST & Validation Clients

### curl
- **Purpose**: Perform rapid local endpoint queries.
- **When to Use**: Testing REST controller APIs during Phase 0 and Phase 1 testing.
- **Command**:
  ```bash
  curl -i -X GET "http://localhost:8080/api/chat?message=Hello"
  ```

### HTTP Client / Postman
- **Purpose**: Formulate complex HTTP requests (especially multi-part files).
- **When to Use**: Testing document upload and structured JSON endpoints.

---

## 6. Testing Frameworks

### JUnit 5 & Mockito
- **Purpose**: Isolated testing, stub configurations, and behavioral mock assertions.
- **When to Use**: Standard fast builds under `mvn test`.
- **Note**: Mock the Spring AI `ChatModel` to run tests without requiring a running Ollama container on local computers.

---

## 7. Future Deployment Tools (Phase 12+)

### Docker / Docker Compose
- **Purpose**: Containerize the Spring Boot jar, local PostgreSQL, and server-side Ollama instances for consistent environments.
- **When to Use**: Preparing packaging profiles for staging or production testing.
