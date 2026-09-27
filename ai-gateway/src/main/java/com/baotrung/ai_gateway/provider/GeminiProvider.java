package com.baotrung.ai_gateway.provider;

import com.baotrung.ai_gateway.dto.ai.LLMMessage;
import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiProvider implements LLMProvider {

    private final RestClient geminiRestClient;
    private final String apiKey;
    private final String defaultModel;

    public GeminiProvider(
            @Qualifier("geminiRestClient") RestClient geminiRestClient,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String defaultModel
    ) {
        this.geminiRestClient = geminiRestClient;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
    }

    @Override
    public String getProviderName() {
        return "GEMINI";
    }

    @Override
    public boolean supportsModel(String model) {
        return model != null && model.startsWith("gemini-");
    }

    @Override
    @SuppressWarnings("unchecked")
    public LLMResponse chat(LLMRequest request) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured"
            );
        }

        String model = request.getModel();

        if (model == null || model.isBlank()) {
            model = defaultModel;
        }

        List<Map<String, Object>> contents = new ArrayList<>();

        for (LLMMessage message : request.getMessages()) {

            Map<String, Object> content = new HashMap<>();

            String role = message.getRole();

            if ("assistant".equalsIgnoreCase(role)) {
                content.put("role", "model");
            } else {
                content.put("role", "user");
            }

            Map<String, Object> part = new HashMap<>();
            part.put("text", message.getContent());

            content.put("parts", List.of(part));

            contents.add(content);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        String uri =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model
                        + ":generateContent";

        System.out.println("Gemini model = " + model);
        System.out.println("Gemini API URL = " + uri);

        Map<String, Object> response = geminiRestClient.post()
                .uri(uri)
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        String contentText = extractText(response);

        Map<String, Object> usageMetadata =
                (Map<String, Object>) response.get("usageMetadata");

        Integer inputTokens = usageMetadata != null
                ? getInteger(
                usageMetadata,
                "promptTokenCount"
        )
                : 0;

        Integer outputTokens = usageMetadata != null
                ? getInteger(
                usageMetadata,
                "candidatesTokenCount"
        )
                : 0;

        String responseId = response.get("responseId") != null
                ? response.get("responseId").toString()
                : null;

        return new LLMResponse(
                contentText,
                model,
                inputTokens,
                outputTokens,
                responseId
        );
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {

        Object candidatesObject = response.get("candidates");

        if (!(candidatesObject instanceof List<?> candidates)) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (Object candidateObject : candidates) {

            if (!(candidateObject instanceof Map<?, ?> candidate)) {
                continue;
            }

            Object contentObject = candidate.get("content");

            if (!(contentObject instanceof Map<?, ?> content)) {
                continue;
            }

            Object partsObject = content.get("parts");

            if (!(partsObject instanceof List<?> parts)) {
                continue;
            }

            for (Object partObject : parts) {

                if (!(partObject instanceof Map<?, ?> part)) {
                    continue;
                }

                Object text = part.get("text");

                if (text instanceof String textValue) {
                    result.append(textValue);
                }
            }
        }

        return result.toString();
    }

    private Integer getInteger(
            Map<String, Object> map,
            String key
    ) {
        Object value = map.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        return 0;
    }
}