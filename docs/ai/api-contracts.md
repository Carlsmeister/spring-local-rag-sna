# AI Workstation: API Contracts

This document outlines the API endpoint contracts for the **local-ai** application. It details path inputs, request-response structures, valid status codes, and implementation phases.

---

## 1. Active MVP Contracts (Career Document Analysis)

### A. Test Chat Connection (Phase 0)
- **Path**: `GET /api/chat?message={message}`
- **Purpose**: Verify local connection to Ollama runtime models.
- **Request Parameters**:
  - `message` (String, Required, length [1, 255])
- **Response Shape** (JSON):
  ```json
  {
    "response": "Hello! I am ready to analyze your career documents."
  }
  ```
- **HTTP Status Codes**:
  - `200 OK`: Model responds successfully.
  - `400 Bad Request`: Parameter missing or empty.
  - `503 Service Unavailable`: Connection to local Ollama host timed out.

### B. Document Upload & Parsing (Phase 1)
- **Path**: `POST /api/documents/upload`
- **Purpose**: Accept multipart files (PDF/DOCX) and extract plain text.
- **Request Payload**:
  - `file` (MultipartFile, Required, size <= 5MB, format PDF or DOCX)
- **Response Shape** (JSON):
  ```json
  {
    "documentId": "4a7b9c2d-8e3f-4a1b-9c8d-7e6f5a4b3c2d",
    "fileName": "Resume_2026.pdf",
    "fileSize": 102450,
    "contentType": "application/pdf",
    "extractedText": "Jane Doe\nSoftware Engineer...",
    "parsedAt": "2026-05-22T11:19:30Z"
  }
  ```
- **HTTP Status Codes**:
  - `201 Created`: Parse complete, document meta established.
  - `400 Bad Request`: Empty stream or invalid file headers (magic bytes).
  - `413 Payload Too Large`: Upload exceeds 5MB size limit.

### C. CV ATS Analysis (Phase 2)
- **Path**: `POST /api/cv/analyze`
- **Purpose**: Send CV text to local AI and return structured ATS metrics.
- **Request Payload** (JSON):
  ```json
  {
    "documentId": "4a7b9c2d-8e3f-4a1b-9c8d-7e6f5a4b3c2d"
  }
  ```
- **Response Shape** (JSON):
  ```json
  {
    "analysisId": "8f7e6d5c-4b3a-2a1b-0c9d-8e7f6a5b4c3d",
    "documentId": "4a7b9c2d-8e3f-4a1b-9c8d-7e6f5a4b3c2d",
    "atsScore": 78,
    "strengths": [
      "Excellent technical skills formatting",
      "Strong work duration metrics"
    ],
    "weaknesses": [
      "No measurable user impact in job descriptions"
    ],
    "recommendations": [
      "Incorporate key deliverables and user metrics."
    ],
    "analyzedAt": "2026-05-22T11:21:00Z"
  }
  ```
- **HTTP Status Codes**:
  - `200 OK`: Analysis successfully compiled.
  - `404 Not Found`: Target document ID does not exist in persistence.

### D. Job Match Analysis (Phase 5)
- **Path**: `POST /api/jobs/match`
- **Purpose**: Match a target document against a job ad description.
- **Request Payload** (JSON):
  ```json
  {
    "documentId": "4a7b9c2d-8e3f-4a1b-9c8d-7e6f5a4b3c2d",
    "jobAdText": "We are seeking a Spring Boot expert who knows Docker..."
  }
  ```
- **Response Shape** (JSON):
  ```json
  {
    "matchPercentage": 72,
    "missingKeywords": ["Docker", "Kubernetes"],
    "matchingStrength": "High matching in core framework requirements.",
    "actionableSuggestions": [
      "Mention your Docker containerization testing experience."
    ]
  }
  ```
- **HTTP Status Codes**:
  - `200 OK`: Semantic similarity match complete.
  - `400 Bad Request`: Empty job description payload.

### E. CV Section Optimizer (Phase 6)
- **Path**: `POST /api/rewrite/section`
- **Purpose**: Return factual, style-optimized sentence optimizations.
- **Request Payload** (JSON):
  ```json
  {
    "documentId": "4a7b9c2d-8e3f-4a1b-9c8d-7e6f5a4b3c2d",
    "originalText": "Responsible for managing the Java database connections.",
    "keywords": ["optimization", "scalability"]
  }
  ```
- **Response Shape** (JSON):
  ```json
  {
    "originalText": "Responsible for managing the Java database connections.",
    "suggestions": [
      {
        "style": "ACTION_ORIENTED",
        "text": "Engineered and optimized Java database connections to maximize data access speeds."
      },
      {
        "style": "METRIC_DRIVEN",
        "text": "Managed Java database connections, maintaining system uptime across production systems."
      },
      {
        "style": "CONCISE",
        "text": "Managed Java database connection optimization."
      }
    ]
  }
  ```
- **HTTP Status Codes**:
  - `200 OK`: Bullet point optimization completed.
  - `422 Unprocessable Entity`: The suggestion generated failed post-processing validation guardrails (hallucinations detected).

---

## 2. Future Authentication & Web Product APIs (Phase 9+ Concept Only)

These endpoints are **strictly deferred** and are listed solely to assist the Architect Agent in package mapping:
- **`GET /api/analyses/{id}`**: Fetch historical review results.
- **`GET /api/users/me`**: Retrieve authenticated profile states.
- **`POST /api/auth/login`**: Acquire secure JWT bearer tokens.
- **`POST /api/auth/logout`**: Terminate active secure sessions.

No active REST controllers or DTO records should reference or define these future endpoints.
