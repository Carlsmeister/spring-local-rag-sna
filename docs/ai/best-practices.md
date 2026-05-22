# AI Workstation: Engineering Best Practices

This document establishes the architectural, structural, and coding best practices for the **local-ai** project.

---

## 1. Java 21 & Modern Coding Standards
- **Use Record Types**: Prefer Java records (`record`) for immutable Data Transfer Objects (DTOs), API requests, and response payloads.
- **Pattern Matching**: Utilize modern pattern matching for `switch` blocks and `instanceof` checks to write highly readable, clean logic.
- **Stream API Operations**: Express data transformations cleanly using Java Streams. Avoid archaic nested loops for sorting and filtering collections.
- **Explicit Var Declarations**: Use `var` for local variable declarations where the type is clearly obvious, keeping code concise.

---

## 2. Spring Boot & Spring AI Best Practices
- **Constructor Injection**: Always use constructor-based dependency injection. Do not use field injection (`@Autowired` on fields) as it complicates unit testing and violates clean coding principles.
  ```java
  // CORRECT
  private final CvAnalysisService cvAnalysisService;
  
  public CvController(CvAnalysisService cvAnalysisService) {
      this.cvAnalysisService = cvAnalysisService;
  }
  ```
- **Thin Controllers**: Keep controllers thin. They should focus exclusively on parsing HTTP inputs, verifying basic parameters, calling isolated services, and returning validated DTO payloads.
- **DTO Isolation**: Do not return raw database JPA entities from controllers. Always translate internal entity configurations into immutable API DTO structures using mappers.
- **Declarative Validation**: Enforce business parameters inside DTO classes using Jakarta Bean Validation (`@NotNull`, `@Size`, `@Min`, `@Max`). Trigger validation by annotating controllers with `@Valid`.

---

## 3. Strict Database & Service Separation
- **No Entities in REST signatures**: Controllers must never accept or return `@Entity` database classes. This protects database designs from API changes.
- **No Controller Database Operations**: Controllers must never directly fetch from or write to JPA repositories. Every read and write transaction must execute through service layers.
- **No Business Logic in Entities**: JPA entity classes should remain clean model definitions mapping to database tables. They must not contain business algorithms, prompt generation logic, or network operations.

---

## 4. AI Prompt Engineering & Safety
- **No Hardcoded Prompts in Code**: Prompt text, formatting rules, and delimiters must not be hardcoded as long string literals inside Java classes. Save prompts in dedicated Markdown/Text resource files under `src/main/resources/prompts/` and resolve them dynamically.
- **No Unvalidated AI Output**: All text received from local LLMs must be strictly validated, deserialized, and checked for schema compliance before being passed up to the controller or returned to the client.
- **Local-First Enforced**: The system must operate under the assumption that LLM calls are local, confidential, and run on private networks. Do not add cloud-hosted dependencies or third-party cloud API adapters without an approved ADR.

---

## 5. Development & Task Management
- **Build One Vertical Slice at a Time**: Focus on completing one specific domain slice (e.g. text ingestion) before beginning structural work on another.
- **Prefer Concrete Implementation Over Early Abstractions**: Do not build complex interface Hierarchies or abstract factory structures for extension modules that are not yet active on the roadmap. Write simple, testable, and refactorable code.
- **Document Architectural Transitions**: If an implementation step requires changing a database relationship or REST endpoint contract, update `docs/ai/architecture.md` and related context files immediately.
- **Small, Highly Traceable Changes**: Commit code in small, logical chunks matching specific task IDs (e.g. `git commit -m "feat(cv): TS-003 integrate Apache Tika parser"`).
