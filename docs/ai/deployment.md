# AI Workstation: Deployment Planning

This document details the deployment architecture, configuration profiles, environment variables, storage adapters, scaling limits, and server-side Ollama deployment strategies for **local-ai**.

---

## 1. Local Development Setup (Current)

During active workstation engineering:
- **Spring Boot Port**: Configured to boot on default port `8080`.
- **Database**: Local PostgreSQL instance with `pgvector` enabled running on `localhost:5432`.
- **Inference Runtime**: Ollama running locally on the developer machine:
  - **Ollama API Port**: `http://localhost:11434`
  - **Active Model**: `gemma4:2eb` (Pre-pulled via `ollama pull gemma4:2eb`)

---

## 2. Spring Profile Matrix

The application externalizes all operational attributes using Spring Profiles.

| Property | `local` (Dev Default) | `test` (CI default) | `prod` (Production) |
| :--- | :--- | :--- | :--- |
| **Server Port** | `8080` | Dynamic (for tests) | `${PORT:8080}` |
| **Ollama URI** | `http://localhost:11434`| Mapped to Mock | `http://ollama-inference-host:11434` |
| **Ollama Model**| `gemma4:2eb` | None | `${OLLAMA_MODEL:gemma4:2eb}` |
| **DB Driver** | `postgresql` | `h2` or Mock | `postgresql` (PGVector Enabled) |
| **CORS Origins**| `http://localhost:3000` | None | `${ALLOWED_CORS_ORIGIN}` |
| **Storage Type**| `LOCAL_DISK` | `IN_MEMORY` | `SECURE_OBJECT_STORE` |

---

## 3. Server-Side Inference Deployment Model

### Zero-Client Requirement
**End-users must never be required to run Ollama or have models pulled locally on their personal browsers or computers.**

### Inference Architecture
In a production deployment, AI inference executes on the server side:
- **Dedicated Inference Host**: Ollama runs inside a Docker container on the application host or on a dedicated remote GPU host (e.g. AWS EC2 with NVIDIA GPU driver configurations).
- **Network Isolation**: The Ollama port `11434` is kept isolated behind a private Virtual Private Cloud (VPC) network. Only the Spring Boot backend container has access to call the inference URI.

---

## 4. Production Security & Infrastructure Controls

Public, internet-facing deployments require hardening measures to protect sensitive user PII:
- **Strict HTTPS**: All traffic must route through TLS 1.3 to secure documents in transit.
- **Mandatory Authentication**: Upload and analysis REST endpoints must be blocked behind secure auth filters (e.g. Spring Security JWT filters).
- **Secure File Storage Strategy**: In development, documents are parsed in scratch directories. In production, files must be stored in secure, private object stores (such as AWS S3 with Server-Side Encryption) rather than persistent server disks.
- **CORS Constraints**: Explicitly lock CORS configs in production to whitelist the official frontend application domain.
- **Secrets Management**: Database passwords, JWT secret keys, and remote host tokens must be injected at runtime using environment variables (e.g. `${DATABASE_PASSWORD}`) or Kubernetes secret volumes. Never commit plain-text credentials to version control.

---

## 5. Performance & Scaling Considerations
- **LLM Inference Latency**: Local models like `gemma4:2eb` require substantial CPU/GPU resources. Standard laptop CPUs can take several seconds to process long resumes. Production architectures must use async processing threads (`@Async` and Deferred HTTP tasks) to prevent request blocking.
- **GPU Scaling**: Production hosts running Ollama must configure GPU acceleration to bring processing speeds down under a second.
- **Memory Overhead**: Spring Boot with Apache Tika and PGVector requires at least **1-2 GB** of RAM. Ollama running `gemma4:2eb` requires at least **4-8 GB** of dedicated system memory.
