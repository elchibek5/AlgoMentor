package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyzeServiceTest {

    @Test
    void parsesStructuredJsonFromLlm() {
        LlmClient llm = mock(LlmClient.class);
        ObjectMapper om = new ObjectMapper();
        AnalyzeService service = new AnalyzeService(llm, om);

        when(llm.analyzeToJson(anyString())).thenReturn(validJson());

        AnalyzeRequest req = new AnalyzeRequest();
        req.setLanguage("java");
        req.setSolution("class Solution {}");
        req.setMode("interview");

        AnalyzeResponse res = service.analyze(req);

        assertNotNull(res);
        assertEquals("O(n)", res.getComplexity().getTime());
        assertEquals("minimum size array", res.getEdgeCases().get(0).getCaseValue());
    }

    @Test
    void retriesOnceWhenFirstResponseIsInvalidJson() {
        LlmClient llm = mock(LlmClient.class);
        AnalyzeService service = new AnalyzeService(llm, new ObjectMapper());

        when(llm.analyzeToJson(anyString()))
                .thenReturn("not json")
                .thenReturn(validJson());

        AnalyzeRequest req = new AnalyzeRequest();
        req.setLanguage("java");
        req.setSolution("class Solution {}");

        AnalyzeResponse response = service.analyze(req);

        assertEquals("O(n)", response.getComplexity().getTime());
        verify(llm, times(2)).analyzeToJson(anyString());
    }

    @Test
    void throwsAfterTwoInvalidResponses() {
        LlmClient llm = mock(LlmClient.class);
        AnalyzeService service = new AnalyzeService(llm, new ObjectMapper());

        when(llm.analyzeToJson(anyString())).thenReturn("bad", "also bad");

        AnalyzeRequest req = new AnalyzeRequest();
        req.setLanguage("java");
        req.setSolution("class Solution {}");

        assertThrows(RuntimeException.class, () -> service.analyze(req));
    }

    private String validJson() {
        return """
                {
                  "summary": ["Assume one solution exists"],
                  "correctness": {
                    "intuition": "Use hash map",
                    "invariants": ["Map contains visited elements"],
                    "proofSketch": "Complement lookup guarantees correctness"
                  },
                  "complexity": {
                    "time": "O(n)",
                    "space": "O(n)",
                    "explanation": "Single pass with map"
                  },
                  "edgeCases": [
                    { "case": "minimum size array", "why": "edge boundary" }
                  ],
                  "pitfalls": ["Using nested loops"],
                  "tests": [
                    { "input": "[2,7]", "expected": "[0,1]", "purpose": "basic" }
                  ],
                  "improvements": ["Add validation"]
                }
                """;
    }
}
