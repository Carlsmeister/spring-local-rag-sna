# local-ai: AI Workstation Entry Point

This is the central entry point and initialization file for the **local-ai** project. All AI assistants (including Gemini / Antigravity CLI) MUST read this file first before proposing plans, reading code, or performing modifications.

## Project Summary
- **Application Name**: local-ai
- **Purpose**: A local-first, privacy-respecting web-based application for CV and cover letter analysis.
- **First Product Module**: **Career Document Analysis** (CV / cover letter text extraction, local AI analysis, ATS/keyword optimization, job matching, and fact-preserving section rewriting).
- **Core Principle**: Local-first runtime AI through Ollama. End users should not be required to run Ollama locally; in a production deployment, inference will run server-side.

## Required Reading Order
Before performing any task, the AI assistant must read files in this exact order:
1. `GEMINI.md` (This file - high-level entry point)
2. `docs/ai/context.md` (Product mission and development vs runtime AI boundaries)
3. `docs/ai/architecture.md` (System layers, package structure, and module design)
4. `docs/ai/roadmap.md` (Implementation phases and milestones)
5. `docs/ai/tasks.md` (Active, backlog, and done tasks)
6. `docs/ai/workflow.md` (AI interaction, multi-agent collaboration, and PR workflows)

## Non-Negotiable Rules
1. **Local-First Runtime**: The runtime AI must remain local-first using Ollama (`gemma4:2eb` model) unless explicitly updated by an Architecture Decision Record (ADR). Cloud AI APIs are strictly forbidden for core runtime features.
2. **Strict Factual Integrity**: Under no circumstances should runtime AI prompts or code invent, fabricate, or embellish achievements, work experience, education, skills, certifications, or dates for the user. Rewrites must preserve the original factual meaning.
3. **Focused MVP Scope**: Focus exclusively on the **Career Document Analysis** module. Do not implement or over-engineer future extension modules (e.g., Social Network Analysis, AI-assisted security analysis) yet.
4. **Thin Controllers & Rich Services**: No business or AI logic is allowed in controllers. Controllers parse requests, delegate to services, and return validated DTOs.
5. **No Blind Abstractions**: Write concrete, highly testable code for the active phase. Do not build generic interfaces or abstract classes for features that are not yet planned or implemented.

## Development Workflow
1. **Read & Check**: Read this file and consult the task board in `docs/ai/tasks.md` to identify the active phase and target task.
2. **Plan**: Propose a localized implementation plan. Write/update tests alongside logic.
3. **Execute**: Modify ONLY relevant files. Use mock-based tests for standard builds; live Ollama tests must be marked optional or integration-only.
4. **Validate**: Run compiler check, execute test suite, and perform schema/validation rules check.
5. **Update**: Update task statuses in `docs/ai/tasks.md`.

## Current Stack
- **Backend**: Java 21, Spring Boot 4.0.6, Spring AI 2.0.0-M6
- **Runtime AI**: Ollama running locally (model: `gemma4:2eb`)
- **Database**: PostgreSQL with PGVector extension
- **Document Ingestion**: Apache Tika (planned)

## Workstation Reference Map
Detailed context and specifications are separated into the following modular files:
- **Product Context**: [context.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/context.md)
- **System Architecture**: [architecture.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/architecture.md)
- **Phased Roadmap**: [roadmap.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/roadmap.md)
- **Active Task Board**: [tasks.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/tasks.md)
- **Agent Collaboration Workflows**: [workflow.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/workflow.md)
- **AI Factual Guardrails**: [validation.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/validation.md)
- **Security & Upload Constraints**: [security.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/security.md)
- **Testing Strategy**: [testing.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/testing.md)
- **Engineering Best Practices**: [best-practices.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/best-practices.md)
- **Coding Styleguide**: [styleguide.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/styleguide.md)
- **AI Skills Registry**: [skills.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/skills.md)
- **Workstation Tools**: [tools.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/tools.md)
- **Runtime Prompts & Injection Defense**: [prompts.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/prompts.md)
- **Domain Entities & Schema Draft**: [domain-model.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/domain-model.md)
- **API Endpoint Contracts**: [api-contracts.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/api-contracts.md)
- **Deployment Strategy**: [deployment.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/deployment.md)
- **Observability & Logging**: [observability.md](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/observability.md)
- **Architecture Decisions**: [ADR-0001](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/decisions/ADR-0001-local-first-ai.md) | [ADR-0002](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/decisions/ADR-0002-web-product-direction.md)
