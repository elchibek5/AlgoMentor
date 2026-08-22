package com.algomentor.backend;

import com.algomentor.backend.llm.DemoLlmClient;
import com.algomentor.backend.llm.LlmClient;
import com.algomentor.backend.llm.OpenAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the LLM implementation at startup.
 *
 * <p>A single factory method decides, rather than competing {@code @Conditional} beans, so
 * the selection is deterministic and the active mode can be reported on /api/health.
 */
@Configuration
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public LlmConfig(
            @Value("${openai.apiKey:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model,
            @Value("${openai.baseUrl:https://api.openai.com/v1}") String baseUrl
    ) {
        this.apiKey = resolveApiKey(apiKey);
        this.model = model;
        this.baseUrl = baseUrl;
    }

    @Bean
    public LlmClient llmClient() {
        if (demoMode()) {
            log.warn("""
                    
                    ────────────────────────────────────────────────────────────
                     AlgoMentor is running in DEMO MODE.
                     Analyses are generated offline and are not real reviews.
                    
                     To enable real analysis:
                       1. Add OPENAI_API_KEY=sk-... to .env.local
                       2. Restart the backend
                    ────────────────────────────────────────────────────────────
                    """);
            return new DemoLlmClient();
        }
        log.info("AlgoMentor is running in LIVE MODE using model '{}'.", model);
        return new OpenAiClient(apiKey, model, baseUrl);
    }

    public boolean demoMode() {
        return apiKey == null || apiKey.isBlank();
    }

    public String model() {
        return model;
    }

    /** Falls back through the places a key can legitimately come from. */
    private static String resolveApiKey(String configured) {
        if (configured != null && !configured.isBlank()) return configured;

        String fromEnv = System.getenv("OPENAI_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;

        String fromProps = System.getProperty("openai.apiKey");
        if (fromProps != null && !fromProps.isBlank()) return fromProps;

        return null;
    }
}
