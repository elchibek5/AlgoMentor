package com.algomentor.backend;

import io.github.cdimascio.dotenv.Dotenv;

public final class DotenvBootstrap {
    private DotenvBootstrap() {}

    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String key = dotenv.get("OPENAI_API_KEY");
        if (key != null && !key.isBlank()) {
            System.setProperty("openai.apiKey", key);
        }
        System.out.println("Dotenv loaded key? " + (key != null && !key.isBlank()));

    }
}
