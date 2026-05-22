# ADR-0002: Web Product Focus with API-First Execution

- **Status**: Accepted
- **Date**: 2026-05-22

---

## 1. Context
The target demographic for **local-ai** is job-seekers, interns, and early-career applicants. These users expect highly polished, interactive, and responsive browser-based user experiences. They typically interact with applications through modern web pages rather than terminal prompts or command-line scripts.

To support this product direction, the system architecture must be designed to accommodate future web portal integrations, multi-user accounts, file uploads, and session management. However, building a complex frontend user interface (UI) alongside a volatile, evolving AI backend pipeline can lead to high refactoring overheads.

---

## 2. Decision
We will design the application as a **future web-based product, while implementing clean backend/API layers first**.

- **API-First Framework**: Initial execution focus is directed at stabilizing Spring Boot REST endpoints, document parsers, database entities, and RAG pipelines.
- **Web-Ready Design**: All architectural patterns, controllers, CORS configurations, security bounds, and error payloads must be designed under the assumption that a browser frontend (e.g. React or Next.js) will interface with them.
- **Frontend Decoupling**: We will **defer writing standard browser UI components** until the core backend document parsing and local AI analysis pipelines have reached production-grade stability (Phase 9 of the Roadmap).

---

## 3. Consequences

### Benefits
- **Developer Focus**: Engineers can stabilize and test the core AI, chunking, and validation guardrails using automated mock suites without carrying UI layout overheads.
- **Clean Architectural Separation**: API contracts (DTO structures) are fully defined and isolated, preventing internal database schema changes from breaking the user-facing interface.
- **Web Scaling Readiness**: Building with stateless controllers, DTO mapping patterns, and Spring Security filters ensures the system remains scalable for secure web hosting environments.

### Trade-offs
- **Deferred Visual Feedback**: The product remains a set of REST endpoints during early implementation phases, requiring testing via CLI utilities (`curl`), integration test suites, or API clients instead of visual browser pages.

---

## 4. Alternatives Considered

### Alternative A: Backend-Only Terminal CLI Tool
- **Why Rejected**: Job-seekers expect modern web applications. A command-line script restricts accessibility to technical users, defeating the product vision of assisting early-career applicants.

### Alternative B: Local Desktop UI Tool (e.g. JavaFX)
- **Why Rejected**: High development friction. Desktop distributions require local system configurations and local installations of Ollama, contradicting the requirement that end users must not be forced to run complex system model setups.

### Alternative C: Simultaneous Full-Stack Delivery
- **Why Rejected**: Introduces extreme complexity. Modifying AI prompt expectations or parsing structures would require rewriting API routes and visual interfaces simultaneously, slowing down initial vertical slice iterations.
