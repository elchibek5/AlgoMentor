package com.algomentor.backend;

import jakarta.validation.constraints.NotBlank;

public class AnalyzeRequest {

    @NotBlank
    private String language;

    private String problem;

    @NotBlank
    private String solution;

    private String constraints;

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
