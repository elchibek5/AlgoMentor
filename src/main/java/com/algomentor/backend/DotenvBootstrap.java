package com.algomentor.backend;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Set;

/**
 * Loads local environment files before Spring starts, so {@code @Value} placeholders can
 * see them.
 *
 * <p>{@code .env.local} wins over {@code .env}, matching the convention the setup docs use.
 * Values still present from the shipped template are ignored, so a half-finished copy of
 * {@code .env.local.example} falls back to demo mode instead of failing against the real
 * OpenAI API with a confusing 401.
 */
public final class DotenvBootstrap {

    private static final String KEY = "OPENAI_API_KEY";

    /** Values carried over from the example files, which mean "not configured yet". */
    private static final Set<String> PLACEHOLDERS = Set.of(
            "your-openai-api-key-here",
            "your_key_here",
            "sk-...",
            "changeme"
    );

    private DotenvBootstrap() {}

    public static void load() {
        // A real environment variable always wins over a file.
        if (isUsable(System.getenv(KEY))) return;

        String key = read(".env.local");
        if (key == null) {
            key = read(".env");
        }
        if (key != null) {
            System.setProperty("openai.apiKey", key);
        }
    }

    private static String read(String filename) {
        String value = Dotenv.configure()
                .filename(filename)
                .ignoreIfMissing()
                .load()
                .get(KEY);
        return isUsable(value) ? value.trim() : null;
    }

    private static boolean isUsable(String value) {
        return value != null
                && !value.isBlank()
                && !PLACEHOLDERS.contains(value.trim().toLowerCase());
    }
}
