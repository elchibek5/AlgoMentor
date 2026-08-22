package com.algomentor.backend.llm;

import com.algomentor.backend.LlmUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAiClient implements LlmClient {

    private final String apiKey;
    private final String model;
    private final String baseUrl;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper om = new ObjectMapper();

    public OpenAiClient(String apiKey, String model, String baseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAiClient requires a non-blank API key.");
        }
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
    }

    @Override
    public String analyzeToJson(String prompt) {
        return sendPrompt(prompt);
    }

    @Override
    public String chat(String prompt) {
        return sendPrompt(prompt);
    }

    private String sendPrompt(String prompt) {
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
                throw new LlmUnavailableException(describe(resp.statusCode()), null);
            }

            JsonNode root = om.readTree(resp.body());
            return extractOutputText(root, resp.body());

        } catch (LlmUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmUnavailableException(
                    "Could not reach the model provider. Check your network connection.", e);
        }
    }

    /** Turns a provider status code into something the caller can actually act on. */
    private static String describe(int status) {
        return switch (status) {
            case 401, 403 -> "The OPENAI_API_KEY was rejected. Check that the key in .env.local is "
                    + "valid and still active.";
            case 429 -> "Rate limit or quota exceeded on your OpenAI account. Check your billing "
                    + "and usage limits, then retry.";
            case 404 -> "The configured model was not found. Check the 'openai.model' property.";
            default -> status >= 500
                    ? "The model provider is having trouble right now. Please retry shortly."
                    : "The model provider rejected the request (HTTP " + status + ").";
        };
    }

    private String extractOutputText(JsonNode root, String rawBody) {
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
            if (!sb.isEmpty()) {
                return sb.toString().trim();
            }
        }

        JsonNode outputText = root.get("output_text");
        if (outputText != null && outputText.isTextual()) {
            return outputText.asText().trim();
        }

        throw new RuntimeException("No output text found in response: " + rawBody);
    }

}
