# AlgoMentor Backend

AlgoMentor is a Spring Boot backend API that analyzes algorithm solutions and returns a **strict, structured JSON report** covering correctness, complexity, edge cases, pitfalls, tests, and improvements.

It is built as a **production-minded, resume-ready backend project**, focusing on clean architecture, validation, reliability, and safe LLM integration.

---

## 🚀 What It Does

- Accepts pasted algorithm solutions (e.g. LeetCode-style)
- Uses an LLM to analyze the solution
- Returns **predictable JSON** (not free-form text)
- Designed for **interview preparation and learning**

---

## 🧠 Output Structure

Each analysis returns:
- Summary & assumptions
- Correctness (intuition, invariants, proof sketch)
- Time & space complexity
- Edge cases
- Common pitfalls
- Example tests
- Possible improvements

All responses follow a strict schema and are validated before returning.

---

## 🏗 Architecture

```

┌──────────────────┐
│ AnalyzeController│  REST API + validation
└────────┬─────────┘
│
┌────────▼─────────┐
│ AnalyzeService   │  Prompting, retry logic,
│                  │  JSON parsing & validation
└────────┬─────────┘
│
┌────────▼─────────┐
│   LlmClient      │  Interface (decoupled)
└───────┬──────────┘
│
┌──────▼──────────┐
│ OpenAiClient    │  Real OpenAI API
└─────────────────┘
│
┌──────▼──────────┐
│ NoopLlmClient   │  Fallback (no API key)
└─────────────────┘

```

**Key design goals**
- Clear separation of concerns
- Testable core logic
- No vendor lock-in at service layer

---

## 📡 API

### Analyze Solution
```

POST /api/analyze

````

**Request**
```json
{
  "language": "java",
  "mode": "interview",
  "problem": "Two Sum",
  "constraints": "n up to 1e5",
  "solution": "class Solution { }"
}
````

**Response**

```json
{
  "summary": ["..."],
  "correctness": { "...": "..." },
  "complexity": { "time": "O(n)", "space": "O(n)" },
  "edgeCases": [{ "case": "...", "why": "..." }],
  "pitfalls": ["..."],
  "tests": [{ "...": "..." }],
  "improvements": ["..."]
}
```

---

## 🛡 Reliability & Quality

* JSON-only prompting (no markdown, no free text)
* Automatic retry if model returns invalid JSON
* Request validation with clear `400` responses
* Controlled `500` errors (no raw stack traces)
* Unit tests for validation and JSON parsing
* Deterministic output (`temperature = 0`)

---

## 🧪 Testing

```bash
./mvnw test
```

Tests cover:

* Controller validation
* Service-level JSON parsing with mocked LLM

---

## ▶ Run Locally

```bash
# .env (not committed)
OPENAI_API_KEY=your_key_here

./mvnw spring-boot:run
```

Server runs at `http://localhost:8080`.

---

## 🎯 Why This Project

AlgoMentor demonstrates **real backend engineering concerns**:

* API contracts over free-form text
* Safe AI integration
* Validation, retries, and error handling
* Clean Spring Boot architecture

Built as a **portfolio project for backend / internship roles**, not a demo script.

---

## 👤 Author

**Elchibek Dastanov**
Computer Science Student | Software Engineer
Spring Boot · Java · API Design · LLM Integration
