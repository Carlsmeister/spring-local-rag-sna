# AI Workstation: Coding Styleguide

This styleguide defines the syntax, packaging patterns, exception behaviors, logging standards, and design philosophies to keep the **local-ai** project clean, readable, and uniform.

---

## 1. Core Principles
- **Clear Code Over Clever Code**: Write highly readable, simple, and self-documenting code. Avoid overly complex nested lambdas or cryptic one-liners that hamper readability.
- **Do Not Introduce Advanced Patterns Prematurely**: Do not introduce design patterns (e.g. Visitor, Strategy, Command) until there is a clear requirement for them. Start simple.
- **Prefer Explicit Names**: Avoid generic variable naming schemas. Name variables clearly and descriptively.

---

## 2. Naming Conventions

### General Layout Rules
- **Package Naming**: Always use lower_case with dot separators (e.g., `com.example.demo.cv.service`). Avoid using uppercase or camelCase in package structures.
- **Class & Record Naming**: Always use PascalCase (e.g., `CvAnalysisService`, `UploadResponseDto`).
- **Method & Field Naming**: Always use camelCase (e.g., `parseDocument()`, `atsScore`).
- **Constant Naming**: Always use UPPER_CASE with underscores (e.g., `MAX_UPLOAD_SIZE_BYTES`).

### Structural Component Naming

| Layer / Role | Naming Pattern | Example Class |
| :--- | :--- | :--- |
| **Controller** | `{Domain}Controller` | `CvController`, `DocumentController` |
| **Service Interface**| `{Domain}Service` | `CvAnalysisService` |
| **Service Impl** | `{Domain}ServiceImpl` | `CvAnalysisServiceImpl` |
| **Repository** | `{Domain}Repository` | `DocumentRepository` |
| **Entity (JPA)** | `{Domain}` (singular) | `Document`, `CvAnalysis` |
| **DTO (API)** | `{Domain}{Response/Request}Dto`| `UploadResponseDto`, `AnalysisRequestDto` |
| **Exceptions** | `{Cause}Exception` | `InvalidFileException`, `HallucinationException` |
| **Prompts** | `{UseCase}Prompt` | `CvAnalysisPrompt`, `JobMatchPrompt` |
| **Test Classes** | `{ClassUnderTest}Test` | `TikaParserServiceTest` |

---

## 3. Formatting & Structural Layout

### Class Structure & Formatting
- **Standardized Imports**: Avoid wildcard imports (`import java.util.*`). Organize imports in three distinct logical groups separated by a blank line:
  1. Standard Java library imports (`java.*`, `javax.*`, `jakarta.*`)
  2. Spring Framework and library imports (`org.springframework.*`, etc.)
  3. Internal application imports (`com.example.demo.*`)
- **Strict Indentation**: Use 4 spaces for indentation. Do not use tab characters.
- **Method Length Constraints**: Keep methods short and focused (ideally under 30 lines). If a method performs multiple operations, extract them into helper methods.

---

## 4. Logging & Observability Style
- **Declarative Logger**: Use Lombok's `@Slf4j` annotation to obtain a clean static logger instance:
  ```java
  @Slf4j
  @Service
  public class DocumentServiceImpl implements DocumentService { ... }
  ```
- **LogLevel Guidelines**:
  - `ERROR`: System execution failures, unhandled exceptions, database connection drops (always include the exception object).
  - `WARN`: File validation failures, prompt retry fallbacks, parsing anomalies (no stack trace required).
  - `INFO`: Significant lifecycle operations (e.g., "Document ingestion complete, ID: 123", "Ollama connection established").
  - `DEBUG`: Highly granular transaction steps, query timing metrics, token calculations.

---

## 5. Commenting & Documentation Standards
- **Keep Comments Up to Date**: If code behavior changes, modify the adjacent comments immediately.
- **Javadoc for Key APIs**: Write detailed Javadocs on all public Service interfaces, Controller endpoints, and DTO contracts. Explain inputs, outputs, and checked exceptions.
- **No Commented-Out Code**: Do not leave disabled or commented-out code blocks in classes. Delete unused code completely; rely on Git history for recovery.
