package com.algomentor.backend.llm;

/**
 * Offline stand-in for {@link OpenAiClient}, used when no OPENAI_API_KEY is configured.
 *
 * <p>It returns a schema-valid analysis so the project can be cloned and run with zero
 * setup. The response is derived from simple pattern matching on the submitted code, so
 * it reacts to what you paste — but it is not a real review. Set OPENAI_API_KEY to get
 * genuine analysis.
 */
public class DemoLlmClient implements LlmClient {

    @Override
    public String analyzeToJson(String prompt) {
        String solution = tail(prompt, "solution:");
        String language = firstNonBlank(section(prompt, "language:"), "your language");
        String problem = firstNonBlank(section(prompt, "problem:"), "this problem");

        Shape shape = Shape.of(solution);

        return """
        {
          "summary": [
            "DEMO MODE - this analysis is generated offline, not by a language model.",
            "Set OPENAI_API_KEY in .env.local and restart the backend for real analysis.",
            "Detected a %s written in %s for %s."
          ],
          "correctness": {
            "intuition": "%s",
            "invariants": [
              "Every element is visited at most once per pass over the input.",
              "State accumulated so far stays consistent with the prefix processed so far."
            ],
            "proofSketch": "Demo mode does not prove correctness. A real run walks the loop invariant through initialization, maintenance, and termination to show the returned value is the intended one."
          },
          "complexity": {
            "time": "%s",
            "space": "%s",
            "explanation": "%s"
          },
          "edgeCases": [
            { "case": "Empty input", "why": "Loops never execute, so the fallback return value is what the caller sees." },
            { "case": "Single element", "why": "Any logic comparing pairs has no pair to compare." },
            { "case": "Duplicate values", "why": "Lookup structures keyed by value can overwrite an earlier index." },
            { "case": "Integer overflow on sums", "why": "Adding two large values can wrap before the comparison happens." }
          ],
          "pitfalls": [
            "Returning early before the full input has been scanned.",
            "Off-by-one errors at the final index of the loop.",
            "Mutating the input while iterating over it."
          ],
          "tests": [
            { "input": "[]", "expected": "fallback value", "purpose": "Confirms empty input does not throw." },
            { "input": "[1]", "expected": "fallback value", "purpose": "Confirms single-element input is handled." },
            { "input": "[3, 3]", "expected": "valid answer", "purpose": "Confirms duplicates are not collapsed." },
            { "input": "large random input", "expected": "completes quickly", "purpose": "Confirms the solution scales to the stated constraints." }
          ],
          "improvements": [
            "DEMO MODE - add your OPENAI_API_KEY to get improvements specific to your code.",
            "%s"
          ]
        }
        """.formatted(
                json(shape.kind),
                json(language),
                json(problem),
                json(shape.intuition),
                json(shape.time),
                json(shape.space),
                json(shape.explanation),
                json(shape.improvement)
        );
    }

    @Override
    public String chat(String prompt) {
        return "Demo mode is active, so this reply is canned rather than generated. "
                + "To ask real questions, add OPENAI_API_KEY to .env.local in the backend "
                + "directory and restart the server.";
    }

    /** A coarse read of what the submitted code looks like, used to vary the demo output. */
    private record Shape(String kind, String intuition, String time, String space,
                         String explanation, String improvement) {

        static Shape of(String code) {
            String c = code.toLowerCase();

            if (hasNestedLoop(c)) {
                return new Shape(
                        "brute-force solution with nested iteration",
                        "The code compares elements pairwise, checking each candidate against every other one.",
                        "O(n^2)", "O(1)",
                        "Two nested passes over the input give quadratic time; no auxiliary structure grows with n.",
                        "Replace the inner loop with a hash map lookup to bring this down to O(n) time.");
            }
            if (c.contains("hashmap") || c.contains("dict(") || c.contains("unordered_map")
                    || c.contains("new map") || c.contains("set()")) {
                return new Shape(
                        "hash-based single-pass solution",
                        "The code trades memory for speed, remembering what it has seen so it can answer lookups in constant time.",
                        "O(n)", "O(n)",
                        "One pass over the input with constant-time lookups; the map can hold up to n entries.",
                        "Confirm the hash structure handles duplicate keys the way the problem requires.");
            }
            if (c.contains("sort")) {
                return new Shape(
                        "sort-then-scan solution",
                        "The code establishes an ordering first, then exploits that order to avoid re-comparing elements.",
                        "O(n log n)", "O(n)",
                        "Sorting dominates the runtime; space depends on whether the sort is in-place.",
                        "If the input is already sorted, or only partially unsorted, a linear scan may be enough.");
            }
            if (hasRecursion(c)) {
                return new Shape(
                        "recursive solution",
                        "The code breaks the problem into smaller instances of itself and combines the sub-answers.",
                        "O(n)", "O(n)",
                        "Each element is handled once; the call stack grows with recursion depth.",
                        "Add memoization if sub-problems repeat, or convert to iteration to avoid stack overflow on deep inputs.");
            }
            return new Shape(
                    "single-pass solution",
                    "The code walks the input once, updating its answer as it goes.",
                    "O(n)", "O(1)",
                    "One linear scan with a fixed number of variables held in memory.",
                    "Verify the fallback return value is correct when no answer is found.");
        }

        private static boolean hasNestedLoop(String c) {
            int first = indexOfLoop(c, 0);
            return first >= 0 && indexOfLoop(c, first + 1) >= 0;
        }

        private static int indexOfLoop(String c, int from) {
            int f = c.indexOf("for", from);
            int w = c.indexOf("while", from);
            if (f < 0) return w;
            if (w < 0) return f;
            return Math.min(f, w);
        }

        private static boolean hasRecursion(String c) {
            return c.contains("return helper") || c.contains("return solve")
                    || c.contains("return dfs") || c.contains("recurs");
        }
    }

    /**
     * Pulls the final field out of the prompt. The submitted code is the last thing the
     * prompt carries and spans many lines, so it runs to the end rather than to a newline.
     */
    private static String tail(String prompt, String key) {
        if (prompt == null) return "";
        int at = prompt.indexOf(key);
        return at < 0 ? "" : prompt.substring(at + key.length()).trim();
    }

    /** Pulls a single-line "key: value" field out of the prompt the service builds. */
    private static String section(String prompt, String key) {
        if (prompt == null) return "";
        int at = prompt.indexOf(key);
        if (at < 0) return "";
        int start = at + key.length();
        int end = prompt.indexOf('\n', start);
        return (end < 0 ? prompt.substring(start) : prompt.substring(start, end)).trim();
    }

    private static String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /** Escapes a value for safe interpolation into the JSON string literal above. */
    private static String json(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n', '\r' -> sb.append(' ');
                case '\t' -> sb.append("  ");
                default -> {
                    if (ch >= 0x20) sb.append(ch);
                }
            }
        }
        return sb.toString();
    }
}
