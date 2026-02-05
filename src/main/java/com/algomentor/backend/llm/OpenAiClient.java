package com.algomentor.backend.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "openai.apiKey")
public class OpenAiClient implements LlmClient {

    private final String apiKey;
    private final String model;
    private final String baseUrl;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public OpenAiClient(
            @Value("${openai.apiKey:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model,
            @Value("${openai.baseUrl:https://api.openai.com/v1}") String baseUrl
    ) {

        String resolved = apiKey;

        if (resolved == null || resolved.isBlank()) {
            resolved = System.getenv("OPENAI_API_KEY");
        }

        if (resolved == null || resolved.isBlank()) {
            resolved = System.getProperty("OPENAI_API_KEY");
        }

        if (resolved == null || resolved.isBlank()) {
            resolved = loadKeyFromEnvFile();
        }

        if (resolved == null || resolved.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY not loaded (check .env file location / working directory)");
        }

        this.apiKey = resolved;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    @Override
    public String analyzeToJson(String prompt) {
        try {
            String body = om.createObjectNode()
                    .put("model", model)
                    .put("input", prompt)
                    .put("temperature", 0)
                    .toString();

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/responses"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("OpenAI error " + resp.statusCode() + ": " + resp.body());
            }

            JsonNode root = om.readTree(resp.body());

// 1) New Responses format: output -> [ { content: [ { type: "output_text", text: "..." } ] } ]
            JsonNode output = root.get("output");
            if (output != null && output.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode item : output) {
                    JsonNode content = item.get("content");
                    if (content != null && content.isArray()) {
                        for (JsonNode c : content) {
                            String type = c.path("type").asText("");
                            if ("output_text".equals(type)) {
                                sb.append(c.path("text").asText(""));
                            }
                        }
                    }
                }
                if (!sb.isEmpty()) return sb.toString().trim();
            }

            // 2) Backward/alternate: output_text (some SDKs flatten it)
            JsonNode outputText = root.get("output_text");
            if (outputText != null && outputText.isTextual()) return outputText.asText().trim();

            throw new RuntimeException("No output text found in response: " + resp.body());


        } catch (Exception e) {
            throw new RuntimeException("OpenAI request failed: " + e.getMessage(), e);
        }
    }

    private String loadKeyFromEnvFile() {
        try {
            Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();

            while (dir != null) {
                Path candidate = dir.resolve(".env");
                if (Files.exists(candidate)) {
                    Dotenv dotenv = Dotenv.configure()
                            .directory(dir.toString())
                            .ignoreIfMissing()
                            .load();
                    return dotenv.get("OPENAI_API_KEY");
                }
                dir = dir.getParent();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}
