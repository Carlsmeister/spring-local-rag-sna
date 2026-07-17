# AI Workstation: Deployment Planning

This document details the deployment architecture, configuration profiles, environment variables, storage adapters, scaling limits, and server-side Ollama deployment strategies for **local-ai**.

---

## 1. Local Development Setup (Current)

During active workstation engineering:
- **Spring Boot Port**: Configured to boot on default port `8080`.
- **Database (Dockerized)**: PostgreSQL 16 with `pgvector` enabled, running in a Docker container mapped to `localhost:5432`. This setup guarantees environment consistency and ease of setup.
  - **Docker Compose File**: `docker-compose.yml` at the project root.
  - **Database Image**: `pgvector/pgvector:pg16`
  - **Database Name**: `localai`
  - **Database User**: `postgres`
- **Inference Runtime**: Ollama running locally on the developer machine:
  - **Ollama API Port**: `http://localhost:11434`
  - **Active Model**: `gemma2:2b` (Pre-pulled via `ollama pull gemma2:2b`)

### Managing the Database Container
Run the following commands from the project root:
- **Start Database**: `docker compose up -d postgres` (Starts PostgreSQL in detached background mode)
- **Stop Database**: `docker compose down` (Stops the container; data remains safe inside the Docker volume)
- **View Logs**: `docker compose logs -f postgres` (Follow database logs for troubleshooting)
- **Destroy Volume**: `docker compose down -v` (Stops the container and **wipes** all data - use only when resetting the database state)

### Data Persistence (Volumes)
By default, Docker container filesystems are ephemeral. To prevent data from disappearing when containers are stopped or removed, the `docker-compose.yml` mounts a persistent Docker volume named `pgdata` to the PostgreSQL data directory (`/var/lib/postgresql/data`). This ensures that your schemas, tables, and career documents are safely preserved across server restarts.


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

---

## 6. Hobby / Amateur Deployment Architecture (Docker Compose)

For small-scale, hobby, or amateur hosting where a managed database service (like AWS RDS) is too expensive, you can deploy the PostgreSQL database inside a Docker container on your virtual private server (VPS).

### Recommended Hobby Stack
- **Docker Compose Setup**: Define both your Spring Boot backend and your PostgreSQL database in a single `docker-compose.yml` file.
- **Persistent Data Volume**: Define a named volume (e.g., `pgdata`) exactly like in local development. Point it to `/var/lib/postgresql/data`.
- **Database Backups**: Set up a simple nightly cron job on your host server to run `pg_dump` inside the container and back up the output to a safe off-site location (e.g., a local folder or a cheap object storage bucket):
  ```bash
  docker exec -t local-ai-db pg_dumpall -c -U postgres > /path/to/backups/backup_$(date +%Y%m%d).sql
  ```
- **Port Security**: **Do not** map port `5432:5432` to the public internet on your server. Instead, only expose port `5432` inside the Docker internal network so that only the Spring Boot backend container can connect to it. Keep database access private.

