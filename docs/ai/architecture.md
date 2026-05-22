# AI Workstation: Architecture

This document defines the architectural patterns, packaging layouts, data flows, and layer responsibilities for the **local-ai** application.

---

## High-Level System Architecture

The application follows a clean, modular layered architecture. Each layer has specific responsibilities and strict rules about dependency directions. 

```
+-------------------------------------------------------------+
|                     Client Layer (Web API)                  |
|          Controllers, REST Endpoints, Spring Security       |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                      Business Layer                         |
|   Services (Document, CV Analysis, Job Matching, Rewrites)  |
+--------------+---------------+--------------+---------------+
               |               |              |
               v               v              v
+--------------+---+   +-------+------+   +---+---------------+
|   Ingestion &    |   |  Local AI &  |   |  Data Access &    |
|   Parser Layer   |   |  RAG Engine  |   | Persistence Layer |
|  (Apache Tika)   |   | (Spring AI & |   |   (Spring Data    |
|                  |   |  PGVector)   |   |  JPA, Postgres)   |
+------------------+   +-------+------+   +-------------------+
                               |
                               v
                       +-------+------+
                       |  Ollama Model|
                       |  (gemma4:2eb)|
                       +--------------+
```

---

## Recommended Modular Package Structure

To ensure that the **Career Document Analysis** vertical slice remains clean and modular while allowing for future extensions, we adopt a domain-driven and technical hybrid layout under the base package: `com.example.demo`

```
com.example.demo/
│
├── LocalAiApplication.java
│
├── config/                  # General app configs (Tika, Database, Async)
├── security/                # Spring Security setup, CORS, future JWT filters
│
├── controller/              # General REST entry points
├── service/                 # Shared system utility services
├── dto/                     # Shared base Data Transfer Objects
├── model/                   # Shared core domain models (base entities)
├── repository/              # Shared base repositories
│
├── ai/                      # Spring AI & LLM components
│   ├── prompt/              # System prompt managers and templates
│   ├── rag/                 # PGVector RAG integration, chunking, and embedding
│   ├── workflow/            # Future AI Agent orchestration and sequential steps
│   └── validation/          # Factual validation guardrails & LLM schema checker
│
├── document/                # Base document ingestion engine
│   ├── parser/              # Apache Tika text extractor
│   └── storage/             # File storage interfaces and local/disk storage implementation
│
├── cv/                      # Career Document specific domain
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── model/
│   └── repository/
│
├── job/                     # Job Advertisement specific domain
│   ├── service/
│   ├── dto/
│   ├── model/
│   └── repository/
│
├── analysis/                # High-level CV analysis results and scores
│   ├── service/
│   └── dto/
│
├── rewrite/                 # Interactive resume sentence optimization engine
│   └── service/
│
├── user/                    # Future user management module
│   ├── service/
│   └── model/
│
└── web/                     # Future web portal-specific layout elements
```

### Architectural Extension Modularity
To accommodate future modules without breaking the base layout, those modules will reside in dedicated, isolated root packages alongside `cv` and `job`:
- `com.example.demo.socialnetwork/` (Social network analysis, future)
- `com.example.demo.securityanalysis/` (AI-driven malicious upload scanners, future)
- `com.example.demo.risk/` (Fraud / credibility analysis, future)

No active class in the `cv` package should import or depend on packages within these future extension areas.

---

## Key System Modules & Responsibilities

### 1. Ingestion & Text Parsing (`document`)
- **Responsibility**: Accepts multipart file uploads. Strictly validates mime types and file sizes before passing files down.
- **Library**: Uses **Apache Tika** (`Tika` instance) to extract clean, unformatted raw text and metadata from PDF and DOCX documents.
- **Storage**: Integrates with local file storage in development and allows abstraction for cloud-like private storage in the future.

### 2. Spring AI & Local Inference (`ai`)
- **Responsibility**: Bridges the Spring Boot logic with local **Ollama** using the `ChatModel` adapter.
- **Model**: `gemma4:2eb`.
- **System Prompts**: Configures prompt templates using external resources (Markdown or custom files) rather than hardcoding long prompt strings inside Java classes.

### 3. RAG Engine & Embedding (`ai/rag`)
- **Responsibility**: Chunks extracted document text using a semantic or recursive character text splitter. Generates vector embeddings using the local Ollama embedding model and persists them to **PGVector** using Spring AI's `VectorStore` adapter.
- **Retrieval**: Performs semantic distance calculations to pull contextually relevant CV sections for job ad matching.

### 4. Structured Output & Validation (`ai/validation`)
- **Responsibility**: Instructs the LLM to output pure JSON compliant with specific schema requirements, parses the result safely into validated DTOs, and executes secondary post-processing checks to guarantee that no factual details were hallucinated.

### 5. CV Analysis & Matching (`cv`, `job`, `analysis`)
- **Responsibility**: orchestrates CV metrics, calculates ATS keyword coverage, performs semantic comparisons against Job Ads, and generates concrete recommendations.

### 6. Rewrite Engine (`rewrite`)
- **Responsibility**: Accepts user requests to optimize specific document sentences. Feeds context to local LLMs with strict rules preventing the creation of new dates, experiences, or skills.

---

## Data Flow Diagrams (Text-Based)

### Text Ingestion & Parsing Flow
```
User Upload -> CVController -> DocumentService -> ApacheTikaParser -> Raw Text Output -> ExtractedTextDTO -> Client
```

### AI Vector Embedding & Indexing Flow
```
Raw Text -> TextSplitter (Chunks) -> EmbeddingModel (Vectors) -> PGVector Store (Persistent Index)
```

### Contextual Retrieval & Analysis Flow (RAG)
```
Job Ad Text -> Semantic Search -> PGVector Store -> Relevant CV Chunks -> System Prompt Merger -> Ollama ChatModel -> DTO Validation -> Client
```

---

## Dependency Direction & Layering Rules

1. **Dependency Inversion**: High-level layers must depend on abstractions. Controllers depend on Service Interfaces; Services depend on Repository Interfaces.
2. **Business Domain Integrity**: Service classes house the business domain rules. Controllers are restricted from implementing business rules, direct persistence calls, or raw prompt orchestrations.
3. **Strict Validation**: All data crossing borders (Controller inputs, Service inputs, and AI JSON responses) must be explicitly validated using `jakarta.validation.constraints` or custom business validation logic.
4. **No Controller Database Access**: Controllers must never directly fetch from or write to repositories. They are thin gateways.
5. **No Database Entities in Controller Signatures**: Database JPA entities (`@Entity`) must not be exposed directly in Controller request/response bodies. DTOs must represent the API contracts.

---

## Deployment-Aware Architecture Notes
- **Local Development**: Standard configuration targets local Ollama (`http://localhost:11434`) and local PostgreSQL.
- **Web Deployment Readiness**: 
  - Database configurations are externalized to environment variables.
  - The model name and connection string for Ollama are dynamically configured through Spring Profiles (`dev`, `prod`).
  - Storage adapters are decoupled through interfaces (`StorageService`) to transition seamlessly from local scratch disks to secure, persistent environments.
