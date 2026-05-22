# AI Workstation: Observability & Logging

This document establishes the logging standards, observability patterns, metrics tracking parameters, and error handling behaviors for the **local-ai** application.

---

## 1. Observability Principles

Our observability model balance two conflicting requirements:
1. **Traceability**: System operators must be able to track request execution paths, compute time overheads, and capture connection failures immediately.
2. **Data Minimization & Privacy**: Since resume documents contain extreme levels of personal user data, application logs **must remain completely free of personal identifiable information (PII)**.

---

## 2. Ingestion & Core Logging Rules

### Logging Exclusions (What NOT to Log)
It is strictly forbidden to output the following to any log file, standard output stream, or third-party monitoring dashboard:
- **No Document Payloads**: Never print raw parsed resume strings or cover letter texts.
- **No Personal Details**: Never output user email addresses, names, locations, phone numbers, or social media links.
- **No Prompt Payloads**: Do not log raw prompt merge files containing candidate resume segments.
- **No Target Sentences**: Do not print specific sentences submitted to the rewrite engine.

### Safe Structural Logging (What to Log)
The application must log structural metadata and metrics:
- **Unique Correlation IDs**: Associate requests with standard Trace IDs (e.g., `MDC` logs maps unique transaction hashes).
- **Execution Timings**: Log computation durations for slow operations (e.g. Apache Tika parsing durations, local LLM inference speeds, vector store query times).
- **Operation States**: Log phase updates (e.g., `"Starting document ingestion. Size: 240KB"`, `"AI analysis successfully serialized. Duration: 1250ms"`).
- **Security Anomalies**: Log file format check failures and validation guardrail blocks (e.g., `"Ingestion blocked: invalid magic byte headers"`, `"Factual validation rejected rewrite suggestions due to hallucinated certifications"`).

---

## 3. Metrics Tracking Matrix

For local and future staging evaluations, we configure logging to track:

| Metric | Target / Unit | Observability Level |
| :--- | :--- | :--- |
| **Document Size** | Bytes | `INFO` |
| **Parsing Duration** | Milliseconds | `INFO` |
| **Ollama Inference Speed**| Milliseconds | `INFO` |
| **Ollama Token Count** | Total tokens / Prompt tokens | `DEBUG` |
| **Similarity Recall Score** | Cosine relevance (0.0 to 1.0) | `DEBUG` |
| **Validation Reject Rate** | Count of rejected hallucinations | `WARN` |

---

## 4. Standardized Error Handling Architecture

To prevent system paths or dependency details from leaking to the outside world:
- **Global Controller Exception Handler**: Implement a central `@ControllerAdvice` bean (`com.example.demo.controller.GlobalExceptionHandler`) that traps all system exceptions (e.g., file parse exceptions, database deadlocks, connection timeouts).
- **Safe API Payloads**: Translate unhandled stack traces into standardized JSON error responses:
  ```json
  {
    "errorCode": "INTERNAL_PROCESSING_ERROR",
    "message": "An error occurred while analyzing your document. Our engineers have been notified.",
    "timestamp": "2026-05-22T11:19:30Z"
  }
  ```
- **Error Stack Trace Logging**: Write the full stack trace of fatal internal errors cleanly to system files using `log.error("Internal processing error. Request ID: " + requestId, exception)` without outputting PII.
