# Architecture Decision Records (ADRs)

This directory houses the Architecture Decision Records (ADRs) for the **local-ai** project.

## What is an ADR?
An ADR is a short text document that captures a significant architectural design decision, its context, the options considered, the chosen path, and its long-term consequences.

## When should an ADR be created?
Create a new ADR when a decision:
- Modifies system layering or packaging boundaries.
- Introduces new third-party frameworks or integrations (e.g., Spring AI, PostgreSQL dependencies).
- Alters database schemas, vector indexing algorithms, or model requirements.
- Modifies secure data storage strategies, privacy policies, or API authentication structures.

## Standard ADR Format
Every ADR must be formatted using these structured sections:
1. **Title**: The identifier and description (e.g., `ADR-0001-local-first-ai`).
2. **Status**: Accepted, Superseded, Proposed, or Rejected.
3. **Context**: What is the problem being addressed? What constraints exist?
4. **Decision**: The selected path. What are we implementing?
5. **Consequences**: What is the impact? What benefits are unlocked? What are the new trade-offs?
6. **Alternatives Considered**: What other options were evaluated? Why were they rejected?

## Active ADR Index
- [ADR-0001: Local-First Runtime AI via Ollama](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/decisions/ADR-0001-local-first-ai.md)
- [ADR-0002: Web Product Focus with API-First Execution](file:///Users/carllundholm/Dokument/Projekt/local-ai/docs/ai/decisions/ADR-0002-web-product-direction.md)
