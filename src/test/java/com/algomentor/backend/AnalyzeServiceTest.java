package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyzeServiceTest {

    @Test
    void parsesStructuredJsonFromLlm() {
        // Mock LLM
        LlmClient llm = mock(LlmClient.class);
        ObjectMapper om = new ObjectMapper();
        AnalyzeService service = new AnalyzeService(llm, om);

        when(llm.analyzeToJson(anyString())).thenReturn("""
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
        """);

        AnalyzeRequest req = new AnalyzeRequest();
        req.setLanguage("java");
        req.setSolution("class Solution {}");
        req.setMode("interview");

        AnalyzeResponse res = service.analyze(req);

        assertNotNull(res);
        assertEquals("O(n)", res.getComplexity().getTime());
        assertEquals(
                "minimum size array",
                res.getEdgeCases().get(0).getCaseValue()
        );
    }
}
