package com.algomentor.backend;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebugController {
    private final Environment env;

    public DebugController(Environment env) {
        this.env = env;
    }

    @GetMapping("/api/debug/config")
    public Map<String, Object> config() {
        String key = env.getProperty("openai.apiKey");
        return Map.of(
                "openai.apiKey.present", key != null && !key.isBlank(),
                "user.dir", System.getProperty("user.dir")
        );
    }
}
