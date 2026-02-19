package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final LlmClient llm;

    public ChatService(LlmClient llm) {
        this.llm = llm;
    }

    public ChatResponse ask(ChatRequest request) {
        String response = llm.chat(buildPrompt(request));
        return new ChatResponse(response == null ? "" : response.trim());
    }

    private String buildPrompt(ChatRequest request) {
        return """
You are AlgoMentor assistant for coding interview practice.
You are helping the user understand their algorithm and improve it.

Rules:
- Be concise and practical.
- If the question is unclear, ask one clarifying question.
- If code is provided, reference bugs, complexity, and improvements.
- Suggest concrete next steps.

User context:
language: %s
problem: %s
constraints: %s
solution: %s
extra_context: %s

User question:
%s
""".formatted(
                safe(request.getLanguage()),
                safe(request.getProblem()),
                safe(request.getConstraints()),
                safe(request.getSolution()),
                safe(request.getContext()),
                safe(request.getQuestion())
        );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
