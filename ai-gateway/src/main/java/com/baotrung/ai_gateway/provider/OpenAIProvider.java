package com.baotrung.ai_gateway.provider;

import com.baotrung.ai_gateway.dto.ai.LLMMessage;
import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIProvider implements LLMProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String defaultModel;

    public OpenAIProvider(
            @Qualifier("openAIRestClient") RestClient openAIRestClient,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String defaultModel
    ) {
        this.restClient = openAIRestClient;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
    }

    @Override
    public String getProviderName() {
        return "OPENAI";
    }

    @Override
    public boolean supportsModel(String model) {
        return model != null && model.startsWith("gpt-");
    }

    @Override
    @SuppressWarnings("unchecked")
    public LLMResponse chat(LLMRequest request) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not configured"
            );
        }

        String model = request.getModel();

        if (model == null || model.isBlank()) {
            model = defaultModel;
        }

        List<Map<String, Object>> input = request.getMessages()
                .stream()
                .map(this::toOpenAIMessage)
                .toList();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", input);

        Map<String, Object> response = restClient
                .post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "OpenAI returned an empty response"
            );
        }

        String content = extractOutputText(response);

        Map<String, Object> usage =
                (Map<String, Object>) response.get("usage");

        Integer inputTokens = usage != null
                ? toInteger(usage.get("input_tokens"))
                : null;

        Integer outputTokens = usage != null
                ? toInteger(usage.get("output_tokens"))
                : null;

        String responseId = response.get("id") != null
                ? response.get("id").toString()
                : null;

        return new LLMResponse(
                content,
                model,
                inputTokens,
                outputTokens,
                responseId
        );
    }

    private Map<String, Object> toOpenAIMessage(
            LLMMessage message
    ) {
        Map<String, Object> content = new HashMap<>();
        content.put("type", "input_text");
        content.put("text", message.getContent());

        Map<String, Object> item = new HashMap<>();
        item.put("role", message.getRole());
        item.put("content", List.of(content));

        return item;
    }

    @SuppressWarnings("unchecked")
    private String extractOutputText(
            Map<String, Object> response
    ) {

        Object outputText = response.get("output_text");

        if (outputText instanceof String text
                && !text.isBlank()) {
            return text;
        }

        Object output = response.get("output");

        if (!(output instanceof List<?> outputList)) {
            throw new IllegalStateException(
                    "OpenAI response contains no output"
            );
        }

        StringBuilder result = new StringBuilder();

        for (Object outputItem : outputList) {

            if (!(outputItem instanceof Map<?, ?> item)) {
                continue;
            }

            Object content = item.get("content");

            if (!(content instanceof List<?> contentList)) {
                continue;
            }

            for (Object contentItem : contentList) {

                if (!(contentItem instanceof Map<?, ?> contentMap)) {
                    continue;
                }

                Object text = contentMap.get("text");

                if (text != null) {
                    result.append(text);
                }
            }
        }

        if (result.isEmpty()) {
            throw new IllegalStateException(
                    "Could not extract text from OpenAI response"
            );
        }

        return result.toString();
    }

    private Integer toInteger(Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(value.toString());
    }
}