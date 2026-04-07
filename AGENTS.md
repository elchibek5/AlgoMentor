# AGENTS.md: AlgoMentor Backend Guide

**AlgoMentor** is a Spring Boot 3 backend that integrates an LLM (OpenAI) to analyze algorithm solutions and return **structured, validated JSON** rather than free-form text. Built for interview prep and learning, it prioritizes reliability and clean architecture.

---

## Architecture & Data Flow

### Core Components

```
AnalyzeController (REST endpoint)
  ↓ validates @RequestBody via AnalyzeRequest (JSR-303)
  ↓
AnalyzeService (business logic)
  ├─ buildPrompt() → meticulous JSON schema + mode normalization
  ├─ calls llm.analyzeToJson(prompt)
  ├─ tryParse() → JSON extraction + deserialization → AnalyzeResponse
  └─ validateResponse() → field-by-field schema checks (enforces structure)
  ↓
LlmClient interface (abstraction)
  ├─ OpenAiClient (real impl, @ConditionalOnProperty)
  └─ NoopLlmClient (fallback, no API key)
  ↓
ApiExceptionHandler (@RestControllerAdvice)
  └─ Maps domain exceptions → predictable JSON error responses
```

**Key design principle:** Services do NOT throw generic `Exception`—they throw domain exceptions (`InvalidModelOutputException`) that handlers map to HTTP status + JSON.

---

## Critical Workflows

### Build & Run
```bash
# Tests (required before commits)
./mvnw test

# Start server (reads .env for OPENAI_API_KEY)
./mvnw spring-boot:run
# Server runs at http://localhost:8080

# Build jar
./mvnw clean package
# Output: target/backend-0.0.1-SNAPSHOT.jar
```

### Local Development Setup
1. **API key loading** (see `DotenvBootstrap.load()` & `OpenAiClient` constructor):
   - Checks `.env` file (walks up directory tree)
   - Falls back to env var `OPENAI_API_KEY`
   - Must set before `@SpringBootApplication` runs
2. **Properties** (`application.properties`):
   - `openai.model`: Defaults to `gpt-4.1-mini`
   - `openai.baseUrl`: Defaults to OpenAI API
   - `app.cors.allowedOrigins`: CSV list (dev: `localhost:5173,localhost:5174`)

### Testing Patterns
- **Mocking:** Use `Mockito.mock(LlmClient.class)` to avoid API calls
- **Schema validation:** `AnalyzeServiceTest` covers both valid JSON and retry logic
- **Example:** `when(llm.analyzeToJson(anyString())).thenReturn(validJson()).thenReturn(fixedJson())`

---

## Project-Specific Conventions

### Response Schema (Strict)
**Do NOT deviate.** The LLM is prompted to return exactly this:
```json
{
  "summary": ["..."],
  "correctness": { "intuition": "...", "invariants": ["..."], "proofSketch": "..." },
  "complexity": { "time": "O(...)", "space": "O(...)", "explanation": "..." },
  "edgeCases": [{ "case": "...", "why": "..." }],
  "pitfalls": ["..."],
  "tests": [{ "input": "...", "expected": "...", "purpose": "..." }],
  "improvements": ["..."]
}
```
- `edgeCases[].case` uses `@JsonProperty("case")` (reserved keyword)
- `complexity.time` and `complexity.space` **must** match regex `^O\(.+\)$`
- All arrays validated as non-empty after parsing

### Request Validation (AnalyzeRequest)
- `language`: @NotBlank, @Size(max=30)
- `solution`: @NotBlank, @Size(max=20000)
- `mode`: Pattern regex, normalized to INTERVIEW|SIMPLE|DEEP (case-insensitive)
- Invalid requests return **400 with field errors**, not 500

### Retry Logic (AnalyzeService)
1. First attempt: `llm.analyzeToJson(prompt)`
2. If invalid JSON: Second attempt with `"Fix the following to be VALID JSON..."`
3. If still invalid: Throw `InvalidModelOutputException` → **502 Bad Gateway**
- **Never retry more than twice** (prevents infinite loops)
- `extractJsonObject()` uses regex-like logic: find first `{`, last `}` to handle markdown wrapping

### Error Handling (ApiExceptionHandler)
| Exception | HTTP | Message |
|-----------|------|---------|
| Validation (@Valid fails) | 400 | Field errors map |
| InvalidModelOutputException | 502 | "Unable to generate a valid analysis" |
| Generic Exception | 500 | "Unexpected server error" (logged) |
| 404 | 404 | not_found |

- **Never expose raw stack traces to clients**
- Validation errors include per-field messages

### LLM Client Abstraction
- `LlmClient` interface has two methods: `analyzeToJson()` and `chat()`
- `OpenAiClient` uses Java HTTP client (not external library) with deterministic `temperature=0`
- Request body keys: `model`, `input`, `temperature` (custom format, not OpenAI chat endpoint)
- Response parsing: Looks for `output[].content[].text` then fallback to `output_text`
- **If API key missing:** `@ConditionalOnProperty(name="openai.apiKey")` prevents OpenAiClient bean creation
  - Falls back to `NoopLlmClient` (always available)

### Prompting Strategy
- Mode normalization: `normalizeMode()` defaults to INTERVIEW if invalid
- Schema in prompt is **human-readable AND JSON-parseable**
- Prompt includes rules like:
  - No markdown, no code fences, output must start with `{` and end with `}`
  - All keys required, even if unknown (use "" or [])
  - Mode-specific behavior (INTERVIEW: concise; DEEP: rigorous)
- Use `safe()` helper to null-check all request fields before interpolation

### CORS Configuration
- Bean-based (not annotation): `CorsConfig` creates `WebMvcConfigurer`
- Routes: `/api/**`
- Methods: GET, POST, OPTIONS
- Origins configurable via `app.cors.allowedOrigins` (no trailing spaces)

---

## Integration Points

### OpenAI API
- **Endpoint:** Customizable via `openai.baseUrl` (default: `https://api.openai.com/v1`)
- **Endpoint path:** `/responses` (not standard OpenAI)
- **Auth:** Bearer token in `Authorization` header
- **Response codes:** Non-2xx treated as error (full body in exception)
- **Output parsing:** Extracting from nested `output[].content[].text` structure

### Frontend Integration
- Expecting endpoints: `POST /api/analyze`, `POST /api/chat`
- CORS origins: Configured for `localhost:5173` (Vite dev) and `5174`
- Request/response are JSON only

### External Dependencies
- **Spring Boot 3.5.10** (Java 21 required, not 17)
- **jackson-databind:** For ObjectMapper JSON operations
- **dotenv-java:** For `.env` file loading (walks directory tree)
- **jakarta.validation:** JSR-303 @Valid annotations
- **JUnit 5 + Mockito:** Test framework (no Testcontainers, no embedded db)

---

## Immediate Productivity Tips

1. **Adding a new endpoint?** Follow the pattern: Controller → Service with @Service → return Java object. ApiExceptionHandler catches all exceptions.
2. **Modifying response schema?** Update 3 places: `AnalyzeResponse` POJO, service validation block, LLM prompt in `buildPrompt()`. Tests will catch mismatches.
3. **Testing LLM-dependent code?** Mock `LlmClient` with `Mockito.when()`. No API calls needed.
4. **Debugging prompt failures?** Check `AnalyzeServiceTest` for valid JSON structure. The service logs nothing—enable Spring debug logging if needed.
5. **Env vars not loading?** Verify `.env` is in project root. `DotenvBootstrap.load()` prints "Dotenv loaded key? true/false" on startup.
6. **Want to add a new mode?** Update `normalizeMode()` switch statement AND update the prompt rules section in `buildPrompt()`.

---

## File Map

| File | Purpose |
|------|---------|
| `AnalyzeController` | REST `/api/analyze` + @Valid |
| `AnalyzeService` | Prompt building, JSON parsing, retry, validation |
| `AnalyzeRequest` | DTO with JSR-303 constraints |
| `AnalyzeResponse` | Response POJO with nested classes (Correctness, Complexity, etc.) |
| `ChatService` | Persona-driven chatbot prompting (no JSON schema) |
| `ApiExceptionHandler` | @RestControllerAdvice mapping exceptions → HTTP/JSON |
| `LlmClient` (interface) | Abstraction for OpenAI or NoopLlmClient |
| `OpenAiClient` | Real HTTP client, conditional on API key |
| `CorsConfig` | Bean-based CORS setup |
| `DotenvBootstrap` | Pre-Spring initialization for .env loading |
| `application.properties` | Spring config (model, baseUrl, CORS origins) |

