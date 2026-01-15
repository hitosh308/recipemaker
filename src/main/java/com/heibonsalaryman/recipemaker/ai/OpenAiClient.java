package com.heibonsalaryman.recipemaker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
    private static final URI RESPONSES_URI = URI.create("https://api.openai.com/v1/responses");
    private static final int MAX_ATTEMPTS = 3;

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiClient(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.getTimeoutMillis()))
            .build();
    }

    public String createJsonResponse(String systemPrompt, String userPrompt) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new IllegalStateException("OpenAI model is not configured");
        }
        String requestBody = buildRequest(systemPrompt, userPrompt);
        HttpRequest request = HttpRequest.newBuilder(RESPONSES_URI)
            .timeout(Duration.ofMillis(properties.getTimeoutMillis()))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return extractOutputText(response.body());
                }
                String summary = summarizeFailure(response.statusCode(), response.body());
                log.warn("OpenAI request failed (attempt {}): {}", attempt, summary);
            } catch (IOException ex) {
                log.warn("OpenAI request failed (attempt {}): {}", attempt, ex.getMessage());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("OpenAI request interrupted (attempt {}): {}", attempt, ex.getMessage());
            }
            sleepBackoff(attempt);
        }
        throw new IllegalStateException("OpenAI request failed after retries");
    }

    private String buildRequest(String systemPrompt, String userPrompt) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", properties.getModel());
        ObjectNode textNode = objectMapper.createObjectNode();
        textNode.set("format", objectMapper.createObjectNode().put("type", "json_object"));
        payload.set("text", textNode);
        ArrayNode input = payload.putArray("input");
        input.add(messageNode("system", systemPrompt));
        input.add(messageNode("user", userPrompt));
        return payload.toString();
    }

    private ObjectNode messageNode(String role, String text) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        ArrayNode content = message.putArray("content");
        content.add(objectMapper.createObjectNode()
            .put("type", "input_text")
            .put("text", text));
        return message;
    }

    private String extractOutputText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode output = root.path("output");
            if (output.isArray()) {
                for (JsonNode outputItem : output) {
                    JsonNode content = outputItem.path("content");
                    if (content.isArray()) {
                        for (JsonNode contentItem : content) {
                            String type = contentItem.path("type").asText("");
                            if ("output_text".equals(type) || "text".equals(type)) {
                                return contentItem.path("text").asText();
                            }
                        }
                    }
                }
            }
            JsonNode fallback = root.path("output_text");
            if (fallback.isTextual()) {
                return fallback.asText();
            }
        } catch (Exception ex) {
            log.warn("Failed to parse OpenAI response payload: {}", ex.getMessage());
        }
        throw new IllegalStateException("OpenAI response did not contain output text");
    }

    private String summarizeFailure(int statusCode, String responseBody) {
        String summary = responseBody == null ? "" : responseBody;
        if (summary.length() > 200) {
            summary = summary.substring(0, 200);
        }
        return String.format(Locale.ROOT, "status=%d body=%s", statusCode, summary);
    }

    private void sleepBackoff(int attempt) {
        try {
            long delay = 300L * attempt * attempt;
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
