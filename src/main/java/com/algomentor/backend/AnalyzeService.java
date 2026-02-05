package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

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

        if (parsed != null) return parsed;

        // Retry once: ask model to fix JSON
        String fixPrompt = "Fix the following to be VALID JSON that matches the required schema exactly. " +
                "Return JSON only.\n\n" + raw;

        String fixed = llm.analyzeToJson(fixPrompt);
        parsed = tryParse(fixed);

        if (parsed != null) return parsed;

        throw new RuntimeException("Model returned invalid JSON twice.");
    }

    private AnalyzeResponse tryParse(String s) {
        try {
            if (s == null) return null;

            String cleaned = extractJsonObject(s.trim());
            if (cleaned == null) return null;

            // Ensure it's valid JSON object
            var node = om.readTree(cleaned);
            if (!node.isObject()) return null;

            // Normalize JSON (removes weird whitespace/trailing junk issues)
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


    private String buildPrompt(AnalyzeRequest req) {
        // Normalize mode into the three supported values
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
