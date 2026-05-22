# AI Workstation: Project Context

This document details the business and technical context of the **local-ai** application. It establishes core principles, scope boundaries, and execution models.

## Application Purpose & Capabilities
**local-ai** is a privacy-focused, local-first web application designed to help job-seekers optimize their CVs, resumes, and cover letters. 

### Core Use Case
1. **Document Ingestion**: Securely upload CVs and cover letters in PDF or DOCX format.
2. **Text Parsing**: Extract raw text and metadata cleanly using Apache Tika.
3. **Local AI Analysis**: Analyze documents to provide:
   - ATS (Applicant Tracking System) compatibility scores.
   - Comprehensive feedback on strengths, structural weaknesses, and grammar.
   - Job ad alignment (analyzing gaps between CV contents and a specific job advertisement).
   - Factual improvement suggestions and keywords that the user should incorporate.
4. **Factual Rewriting**: Suggest rewrites of specific resume bullet points or sentences that align with keywords, **without fabricating or inventing experiences, skills, or dates**.

---

## Architectural Distinctions & Boundaries

### 1. Development AI vs. Runtime AI
We maintain a strict boundary between the tools that build this codebase and the runtime capabilities of the application itself.

| Attribute | Development AI | Runtime AI |
| :--- | :--- | :--- |
| **Tool / Tech** | Gemini / Antigravity CLI | Ollama + `gemma4:2eb` model |
| **Role** | Pair-programs, writes clean Spring Boot code, creates plans. | Exposes inference to the application code via Spring AI. |
| **Location** | Runs in development workspace context. | Runs locally in dev; runs on dedicated server in production. |
| **Data Scope** | Codebase, configuration, planning documents. | User-uploaded files, extracted text, prompts, structured JSON results. |

### 2. Deployment Models & User Experience
The system is designed with a clear transition path from developer workstation to a fully production-grade, web-based product.

```
[Local Development Flow]
Developer Browser / API Client -> Spring Boot Backend -> Ollama (Local Host) -> PostgreSQL / PGVector

[Future Web Production Flow]
End-User Browser -> Deployed Frontend UI -> Spring Boot API Server -> Server-Side Ollama -> Deployed PostgreSQL / PGVector
```

#### Key Deployment Guidelines:
- **Zero-Client AI Requirements**: End users **must not** be required to install Ollama or have local models running on their client machines.
- **Server Inference**: In a production deployment, Ollama or an equivalent model runtime will run on the application host or a dedicated GPU-enabled inference server.
- **Production Hardening**: Shifting from local-first development to public web deployment introduces strict requirements for user accounts, rate limiting, request validation, CORS, token/session management, and data privacy enforcement.

---

## Technical Stack & Starting Point
- **Language & Framework**: Java 21, Spring Boot 4.0.6
- **AI Integration**: Spring AI 2.0.0-M6 (Ollama model adapter, PGVector vector store)
- **Local Runtime AI**: Ollama running `gemma4:2eb`
- **Database & Storage**: PostgreSQL with PGVector extension
- **File Parsing**: Apache Tika (tika-core, tika-parsers-standard-package)
- **Security**: Spring Boot Starter Security (ready to configure)
- **Utilities**: Lombok, Spring Boot DevTools, Jakarta Bean Validation

---

## Scope Boundaries & Future Extension Modules
To avoid premature optimization, our current active scope is limited to the **Career Document Analysis** module. 

Future modules are conceptually acknowledged for architectural modularity but **must not be implemented, planned in detail, or coded** at this stage:
- **Social Network Analysis (SNA)**: Map professional contacts and analyze potential referral paths.
- **AI-Assisted Security Analysis**: Detect prompt injection or malicious instructions embedded in incoming files.
- **Broader Document Intelligence**: Generalized parsing and indexing of tax, legal, or utility documents.
- **Risk Assessment**: Analyze document completeness and credibility profiles.

---

## Core Guiding Principles

### 1. Privacy-First & Local-First AI
The runtime application avoids using external cloud-based AI APIs (e.g., OpenAI, Claude, Gemini API) for core document processing. User CVs contain highly sensitive personal identifiable information (PII) like names, phone numbers, addresses, and employment histories. Processing this data locally guarantees complete data sovereignty and data privacy.

### 2. Strict Factual Guardrails
AI systems are prone to hallucinating. While fabricating work accomplishments might make a CV look stronger, it constitutes fraud. The application must enforce rules (via prompts and validation code) that **never invent skills, certifications, work durations, company names, or academic achievements**. Rewriting suggests optimization of phrasing, not the fabrication of history.

### 3. Web-Ready Architecture
The system is built as a web application from day one. Though initial features may only expose REST APIs, all controllers, services, database configurations, and security filters must be designed under the assumption that a rich browser-based frontend will connect to them in the future.

---

## What the Application Must Never Do
- **NEVER** transmit raw user documents, parsed resume text, or personal profiles to external third-party cloud AI endpoints.
- **NEVER** save plain text or files without verifying user ownership or authorization.
- **NEVER** automatically apply AI rewrites without explicit user review and confirmation.
- **NEVER** return unvalidated AI JSON output that fails schema structural checks.
