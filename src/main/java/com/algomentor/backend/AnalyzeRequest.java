package com.algomentor.backend;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to analyze an algorithm solution")
public class AnalyzeRequest {

    @NotBlank
    @Size(max = 30)
    @Schema(description = "Programming language (e.g., java, python, cpp, javascript)", example = "java")
    private String language;

    @Size(max = 200)
    @Schema(description = "Problem name or title", example = "Two Sum")
    private String problem;

    @NotBlank
    @Size(max = 20000)
    @Schema(description = "The complete solution code", example = "class Solution { public int[] twoSum(...) { } }")
    private String solution;

    @Size(max = 2000)
    @Schema(description = "Problem constraints and additional context", example = "n up to 1e5, return indices of two numbers that add up to target")
    private String constraints;

    @Pattern(regexp = "(?i)^(interview|simple|deep)?$", message = "mode must be INTERVIEW, SIMPLE, or DEEP")
    @Schema(description = "Analysis mode: INTERVIEW (balanced), SIMPLE (quick), DEEP (rigorous)", example = "interview")
    private String mode;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getProblem() { return problem; }
    public void setProblem(String problem) { this.problem = problem; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
