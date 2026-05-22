# AI Workstation: Domain Model Draft

This document maps out the first draft domain model for the **local-ai** application. It identifies the entities, fields, relationships, and execution stages (now vs. later) for the database structure.

---

## 1. Domain Entity Architecture

```
+------------------+             +-------------------+
|   UserProfile    | 1 ------- * |     Document      |
|  (User metadata) |             |  (Upload metadata)|
+------------------+             +---------+---------+
                                           |
                                           | 1 (Composition)
                                           v
                                 +-------------------+
                                 |   ExtractedText   |
                                 |  (Parsed text)    |
                                 +---------+---------+
                                           |
                                           | 1
                                           v
                                 +-------------------+
                                 |    CvAnalysis     |
                                 | (Scores & Recs)   |
                                 +-------------------+
```

---

## 2. Entity Specifications (Career Document Analysis)

### UserProfile (Phase 3)
- **Purpose**: Represents the basic applicant profile.
- **Implementation**: Phase 3.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `firstName` (String, nullable)
  - `lastName` (String, nullable)
  - `email` (String, Unique, Indexed)
  - `createdAt` (LocalDateTime)
- **Relationships**: One-to-Many with `Document`.

### Document (Phase 3)
- **Purpose**: Tracks metadata for uploaded files.
- **Implementation**: Phase 3.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `fileName` (String)
  - `fileSize` (Long)
  - `contentType` (String, e.g. "application/pdf")
  - `documentType` (Enum: `CV`, `COVER_LETTER`)
  - `createdAt` (LocalDateTime)
- **Relationships**: Many-to-One with `UserProfile`, One-to-One with `ExtractedText`.

### ExtractedText (Phase 3)
- **Purpose**: Holds the raw unformatted string parsed from the document stream.
- **Implementation**: Phase 3.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `rawContent` (String / Lob, holds full extracted text)
  - `parsedAt` (LocalDateTime)
- **Relationships**: One-to-One with `Document` (Cascade all deletes).

### CvAnalysis (Phase 3)
- **Purpose**: Stores the results of an ATS score review.
- **Implementation**: Phase 3.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `atsScore` (Integer, 0 to 100)
  - `rawAiResponse` (String / Lob, stores the raw JSON returned from Ollama)
  - `analyzedAt` (LocalDateTime)
- **Relationships**: Many-to-One with `Document` (Allows multiple analysis records per document over time).

### EmbeddingChunk (Phase 4)
- **Purpose**: Represents a semantic segment of a document parsed and vectorized.
- **Implementation**: Phase 4.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `content` (String, logical paragraph content)
  - `embedding` (Vector, multi-dimensional array mapping to PGVector)
  - `chunkIndex` (Integer)
- **Relationships**: Many-to-One with `Document`.

### JobAd (Phase 5)
- **Purpose**: Represents a parsed targeted job advertisement.
- **Implementation**: Phase 5.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `title` (String)
  - `company` (String, nullable)
  - `rawText` (String / Lob)
  - `createdAt` (LocalDateTime)
- **Relationships**: Many-to-One with `UserProfile`.

### JobMatchResult (Phase 5)
- **Purpose**: Persists scores and missing keywords matching a document to a job ad.
- **Implementation**: Phase 5.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `matchScore` (Integer)
  - `missingKeywords` (String, JSON list mapping missing tools)
  - `rawAnalysis` (String / Lob)
  - `matchedAt` (LocalDateTime)
- **Relationships**: Many-to-One with `Document`, Many-to-One with `JobAd`.

### RewriteSuggestion (Phase 6)
- **Purpose**: Cache generated sentence optimizations.
- **Implementation**: Phase 6.
- **Fields**:
  - `id` (UUID, Primary Key)
  - `originalSentence` (String)
  - `suggestedStyle` (Enum: `ACTION_ORIENTED`, `METRIC_DRIVEN`, `CONCISE`)
  - `suggestedText` (String)
  - `createdDate` (LocalDateTime)
- **Relationships**: Many-to-One with `Document`.

---

## 3. Conceptual Future Domain Entities (For Modular Context Only)

These entities are **strictly conceptual** and are listed solely to ensure our current packages allow clean future extensions. **They must not be defined or coded yet**:
- **UserAccount (Phase 8)**: Secure credentials, password hashes, and MFA states.
- **SubscriptionPlan (Phase 9)**: Tiered usage allocations (e.g. Free, Premium, Enterprise).
- **AnalysisProject (Phase 13)**: Grouping multiple CVs, cover letters, and matched jobs into a single "Job Hunt Campaign".
- **SocialNetworkDataset (Phase 13)**: LinkedIn imports, referral nodes, and network path edges.
- **SecurityAnalysisReport (Phase 13)**: Advanced document integrity threat scores and macro virus scanner logs.
