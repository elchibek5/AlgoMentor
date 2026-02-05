package com.algomentor.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AnalyzeResponse {

    private List<String> summary;
    private Correctness correctness;
    private Complexity complexity;
    private List<EdgeCase> edgeCases;
    private List<String> pitfalls;
    private List<TestCase> tests;
    private List<String> improvements;

    public List<String> getSummary() { return summary; }
    public void setSummary(List<String> summary) { this.summary = summary; }

    public Correctness getCorrectness() { return correctness; }
    public void setCorrectness(Correctness correctness) { this.correctness = correctness; }

    public Complexity getComplexity() { return complexity; }
    public void setComplexity(Complexity complexity) { this.complexity = complexity; }

    public List<EdgeCase> getEdgeCases() { return edgeCases; }
    public void setEdgeCases(List<EdgeCase> edgeCases) { this.edgeCases = edgeCases; }

    public List<String> getPitfalls() { return pitfalls; }
    public void setPitfalls(List<String> pitfalls) { this.pitfalls = pitfalls; }

    public List<TestCase> getTests() { return tests; }
    public void setTests(List<TestCase> tests) { this.tests = tests; }

    public List<String> getImprovements() { return improvements; }
    public void setImprovements(List<String> improvements) { this.improvements = improvements; }

    // nested DTOs, Keeping it simple for MVP
    public static class Correctness {
        private String intuition;
        private List<String> invariants;
        private String proofSketch;

        public String getIntuition() { return intuition; }
        public void setIntuition(String intuition) { this.intuition = intuition; }

        public List<String> getInvariants() { return invariants; }
        public void setInvariants(List<String> invariants) { this.invariants = invariants; }

        public String getProofSketch() { return proofSketch; }
        public void setProofSketch(String proofSketch) { this.proofSketch = proofSketch; }
    }

    public static class Complexity {
        private String time;
        private String space;
        private String explanation;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getSpace() { return space; }
        public void setSpace(String space) { this.space = space; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public static class EdgeCase {

        @JsonProperty("case")
        private String caseValue;

        private String why;

        public String getCaseValue() { return caseValue; }
        public void setCaseValue(String caseValue) { this.caseValue = caseValue; }

        public String getWhy() { return why; }
        public void setWhy(String why) { this.why = why; }
    }

    public static class TestCase {
        private String input;
        private String expected;
        private String purpose;

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getExpected() { return expected; }
        public void setExpected(String expected) { this.expected = expected; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }
}
