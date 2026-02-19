package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AnalyzeService {

    private static final Pattern BIG_O_PATTERN = Pattern.compile("^O\\(.+\\)$");

    private final LlmClient llm;
    private final ObjectMapper om;

    public AnalyzeService(@Lazy LlmClient llm, ObjectMapper om) {
        this.llm = llm;
        this.om = om;
    }

    public AnalyzeResponse analyze(AnalyzeRequest req) {
        String prompt = buildPrompt(req);

        String raw = llm.analyzeToJson(prompt);
        AnalyzeResponse parsed = tryParse(raw);

        if (parsed != null) {
            validateResponse(parsed);
            return parsed;
        }

        String fixPrompt = "Fix the following to be VALID JSON that matches the required schema exactly. " +
                "Return JSON only.\\n\\n" + raw;

        String fixed = llm.analyzeToJson(fixPrompt);
        parsed = tryParse(fixed);

        if (parsed != null) {
            validateResponse(parsed);
            return parsed;
        }

        throw new InvalidModelOutputException("Model returned invalid JSON twice.");
    }

    private AnalyzeResponse tryParse(String s) {
        try {
            if (s == null) return null;

            String cleaned = extractJsonObject(s.trim());
            if (cleaned == null) return null;

            var node = om.readTree(cleaned);
            if (!node.isObject()) return null;

            String normalized = om.writeValueAsString(node);

            return om.readValue(normalized, AnalyzeResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractJsonObject(String s) {
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) return null;
        return s.substring(start, end + 1);
    }

    private void validateResponse(AnalyzeResponse response) {
        requireNonEmptyList(response.getSummary(), "summary");
        require(response.getCorrectness() != null, "correctness");
        require(response.getComplexity() != null, "complexity");
        requireNonEmptyList(response.getEdgeCases(), "edgeCases");
        requireNonEmptyList(response.getPitfalls(), "pitfalls");
        requireNonEmptyList(response.getTests(), "tests");
        requireNonEmptyList(response.getImprovements(), "improvements");

        AnalyzeResponse.Correctness correctness = response.getCorrectness();
        requireNonBlank(correctness.getIntuition(), "correctness.intuition");
        requireNonEmptyList(correctness.getInvariants(), "correctness.invariants");
        requireNonBlank(correctness.getProofSketch(), "correctness.proofSketch");

        AnalyzeResponse.Complexity complexity = response.getComplexity();
        requireBigO(complexity.getTime(), "complexity.time");
        requireBigO(complexity.getSpace(), "complexity.space");
        requireNonBlank(complexity.getExplanation(), "complexity.explanation");

        for (int i = 0; i < response.getEdgeCases().size(); i++) {
            AnalyzeResponse.EdgeCase edgeCase = response.getEdgeCases().get(i);
            require(edgeCase != null, "edgeCases[" + i + "]");
            requireNonBlank(edgeCase.getCaseValue(), "edgeCases[" + i + "].case");
            requireNonBlank(edgeCase.getWhy(), "edgeCases[" + i + "].why");
        }

        for (int i = 0; i < response.getTests().size(); i++) {
            AnalyzeResponse.TestCase testCase = response.getTests().get(i);
            require(testCase != null, "tests[" + i + "]");
            requireNonBlank(testCase.getInput(), "tests[" + i + "].input");
            requireNonBlank(testCase.getExpected(), "tests[" + i + "].expected");
            requireNonBlank(testCase.getPurpose(), "tests[" + i + "].purpose");
        }
    }

    private void require(boolean condition, String field) {
        if (!condition) {
            throw new InvalidModelOutputException("Missing or invalid field: " + field);
        }
    }

    private void requireNonBlank(String value, String field) {
        require(value != null && !value.isBlank(), field);
    }

    private void requireNonEmptyList(List<?> value, String field) {
        require(value != null && !value.isEmpty(), field);
    }

    private void requireBigO(String value, String field) {
        requireNonBlank(value, field);
        require(BIG_O_PATTERN.matcher(value.trim()).matches(), field + " must be Big-O format");
    }

    private String buildPrompt(AnalyzeRequest req) {
        String mode = normalizeMode(req.getMode());

        return """
You are AlgoMentor. Return ONLY a single JSON object.
No markdown. No code fences. No explanations outside JSON.
The output MUST start with '{' and end with '}'.

Schema (match exactly, no extra keys):

{
  "summary": ["..."],
  "correctness": { "intuition": "...", "invariants": ["..."], "proofSketch": "..." },
  "complexity": { "time": "O(...)", "space": "O(...)", "explanation": "..." },
  "edgeCases": [ { "case": "...", "why": "..." } ],
  "pitfalls": ["..."],
  "tests": [ { "input": "...", "expected": "...", "purpose": "..." } ],
  "improvements": ["..."]
}

Rules:
- If the problem guarantees a solution exists, do NOT include tests expecting no solution (like []).
- Output MUST be valid JSON.
- Do NOT include any keys outside the schema.
- Use "case" exactly for edge case descriptions.
- Ensure ALL keys exist even if unknown:
  - Use "" for unknown strings
  - Use [] for unknown arrays
- complexity.time and complexity.space must be Big-O strings like "O(n)" or "O(n log n)".
- Put assumptions as items in "summary".
- Mode behavior:
  - INTERVIEW: concise, practical, interviewer-friendly.
  - SIMPLE: beginner-friendly and shorter.
  - DEEP: more rigorous reasoning and invariants.

INPUT:
language: %s
mode: %s
problem: %s
constraints: %s
solution: %s
""".formatted(
                safe(req.getLanguage()),
                mode,
                safe(req.getProblem()),
                safe(req.getConstraints()),
                safe(req.getSolution())
        );
    }

    private String normalizeMode(String mode) {
        if (mode == null) return "INTERVIEW";
        String m = mode.trim().toUpperCase();
        return switch (m) {
            case "INTERVIEW", "SIMPLE", "DEEP" -> m;
            default -> "INTERVIEW";
        };
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}
