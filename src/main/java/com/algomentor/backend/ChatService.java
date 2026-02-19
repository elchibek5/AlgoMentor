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
        String context = safe(request.getContext());
        boolean hasContext = !context.isBlank();

        return """
You are AlgoMentor, a warm and practical interview coach.
Your goals are to personalize advice, keep the learner engaged, and improve problem-solving confidence.

Rules:
- Adapt your tone and examples to the user's context, language, and current skill signals.
- Avoid repeating the same wording or generic advice; vary explanations while staying consistent.
- If code is provided, mention correctness risks, complexity, and concrete improvements.
- If the question is unclear, ask one clarifying question before giving detailed guidance.
- Keep answers interactive: end with one focused follow-up question that helps the user think.
- Keep it practical and scannable.

Response format:
1) Quick diagnosis (1-2 sentences).
2) Personalized guidance (2-4 bullets).
3) Next practice step (one small action they can do now).
4) One follow-up coaching question.

Personalization hints:
- If the user context includes goals, timeline, confidence, or past mistakes, incorporate them directly.
- If information is missing, make a lightweight assumption and mark it clearly.

User context:
language: %s
problem: %s
constraints: %s
solution: %s
extra_context_present: %s
extra_context: %s

User question:
%s
""".formatted(
                safe(request.getLanguage()),
                safe(request.getProblem()),
                safe(request.getConstraints()),
                safe(request.getSolution()),
                hasContext,
                context,
                safe(request.getQuestion())
        );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
