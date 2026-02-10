package com.algomentor.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AnalyzeRequest {

    @NotBlank
    @Size(max = 30)
    private String language;

    @Size(max = 200)
    private String problem;

    @NotBlank
    @Size(max = 20000)
    private String solution;

    @Size(max = 2000)
    private String constraints;

    @Pattern(regexp = "(?i)^(interview|simple|deep)?$", message = "mode must be INTERVIEW, SIMPLE, or DEEP")
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
