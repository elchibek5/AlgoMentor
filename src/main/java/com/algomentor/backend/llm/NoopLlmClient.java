package com.algomentor.backend.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(LlmClient.class)
public class NoopLlmClient implements LlmClient {
    @Override
    public String analyzeToJson(String prompt) {
        throw new IllegalStateException("LLM is not configured. Set OPENAI_API_KEY (or openai.apiKey).");
    }
}
