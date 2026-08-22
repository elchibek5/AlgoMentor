package com.algomentor.backend;

import com.algomentor.backend.llm.DemoLlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Demo mode is what someone sees when they clone the repo and run it without a key, so its
 * output has to survive the same validation a real model response does.
 */
class DemoLlmClientTest {

    private final AnalyzeService service =
            new AnalyzeService(new DemoLlmClient(), new ObjectMapper());

    @Test
    void producesResponseThatPassesFullValidation() {
        AnalyzeResponse res = service.analyze(request("""
                class Solution {
                  public int[] twoSum(int[] nums, int target) {
                    HashMap<Integer, Integer> seen = new HashMap<>();
                    for (int i = 0; i < nums.length; i++) { seen.put(nums[i], i); }
                    return new int[] {-1, -1};
                  }
                }
                """));

        assertNotNull(res);
        assertTrue(res.getSummary().stream().anyMatch(s -> s.contains("DEMO MODE")),
                "demo output must announce itself so nobody mistakes it for a real review");
        assertNotNull(res.getCorrectness().getProofSketch());
        assertTrue(res.getTests().size() >= 2);
    }

    @Test
    void reportsQuadraticTimeForNestedLoops() {
        AnalyzeResponse res = service.analyze(request("""
                for (int i = 0; i < n; i++) {
                  for (int j = i + 1; j < n; j++) { check(i, j); }
                }
                """));

        assertEquals("O(n^2)", res.getComplexity().getTime());
    }

    @Test
    void reportsLinearithmicTimeWhenSolutionSorts() {
        AnalyzeResponse res = service.analyze(request("Arrays.sort(nums); return nums[0];"));

        assertEquals("O(n log n)", res.getComplexity().getTime());
    }

    @Test
    void survivesSolutionContainingJsonBreakingCharacters() {
        AnalyzeResponse res = service.analyze(request("""
                String s = "quote \\" and backslash \\\\ and newline";
                """));

        assertNotNull(res.getComplexity().getTime());
    }

    private static AnalyzeRequest request(String solution) {
        AnalyzeRequest req = new AnalyzeRequest();
        req.setLanguage("java");
        req.setProblem("Two Sum");
        req.setMode("interview");
        req.setSolution(solution);
        return req;
    }
}
