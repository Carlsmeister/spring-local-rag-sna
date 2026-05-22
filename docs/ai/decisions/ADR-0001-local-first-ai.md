# ADR-0001: Local-First Runtime AI via Ollama

- **Status**: Accepted
- **Date**: 2026-05-22

---

## 1. Context
Resume and CV documents contain highly sensitive Personal Identifiable Information (PII) including full names, physical locations, phone numbers, email addresses, and detailed employment/academic histories. Sending this confidential data to public cloud-hosted AI APIs (such as OpenAI GPT or Claude API) poses significant privacy, security, and data sovereignty compliance concerns. 

Additionally, reliance on paid external cloud APIs introduces recurring operational costs, network dependency risks, and rate limit boundaries.

---

## 2. Decision
We will utilize **local-first AI through Ollama** as the primary runtime AI approach for the **local-ai** application.

- **Local Workstation**: During development, the Spring Boot application communicates with a local instance of Ollama (`http://localhost:11434`) running the `gemma4:2eb` model.
- **Server Infrastructure (Production)**: In future public deployments, Ollama (or a dedicated server-side inference container) will host the model on the application server or a dedicated GPU-enabled private server.
- **Zero Client Overhead**: **End users are not required to install Ollama or have local models running on their client machines.** Inference executes server-side, keeping the client integration purely browser-friendly.
- **Possible Future Hybrid Support**: We will build system layers so that a hybrid approach (allowing enterprise clients to opt into secure private cloud endpoints) can be added in the future, but this must only be activated if explicitly decided through a subsequent ADR.

---

## 3. Consequences

### Benefits
- **Absolute Privacy**: Candidate resume strings are kept fully inside our local/private host networks. Zero PII leakage to third-party AI models.
- **Cost Sovereignty**: Running inference locally through CPU/GPU server hosting eliminates pay-per-token API consumption costs.
- **Offline Capabilities**: Developers can code, execute integration tests, and run local AI operations completely offline.

### Trade-offs
- **Hardware Prerequisites**: Executing local model inferences requires sufficient developer memory (at least 8-16GB RAM) and dedicated GPU hardware on server hosts to maintain acceptable response latencies.
- **Model Latency**: Local 4-bit quantized models like `gemma4:2eb` running on CPU hosts can experience slower response latency compared to cloud APIs. This requires building asynchronous processing pipelines (e.g. `@Async` tasks).
- **Setup Complexity**: Requires developer workstations and deployed server containers to configure and maintain a running Ollama installation.

---

## 4. Alternatives Considered

### Alternative A: Pure Cloud API (e.g., OpenAI / Claude)
- **Why Rejected**: Direct violation of the privacy-first principle. Transmitting unencrypted candidate resume PII to public cloud companies raises significant compliance hurdles and introduces operational cost overheads.

### Alternative B: Direct Local Java Inference (e.g., ONNX / Llama.cpp via Java Bindings)
- **Why Rejected**: Extremely high setup complexity. Binding heavy C++ dependencies directly into the JVM container complicates build cycles and reduces host platform portability. Ollama provides a clean, containerized HTTP abstraction layer that matches perfectly with Spring AI libraries.
