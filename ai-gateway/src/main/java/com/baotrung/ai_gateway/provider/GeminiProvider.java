package com.baotrung.ai_gateway.provider;

import com.baotrung.ai_gateway.dto.ai.LLMMessage;
import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import com.baotrung.ai_gateway.exception.ProviderRateLimitException;
import com.baotrung.ai_gateway.exception.ProviderTimeoutException;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiProvider implements LLMProvider {

    private final RestClient geminiRestClient;
    private final String apiKey;
    private final String defaultModel;
    private final int maxRetries;
    private static final Logger log =
            LoggerFactory.getLogger(GeminiProvider.class);

    public GeminiProvider(
            @Qualifier("geminiRestClient") RestClient geminiRestClient,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String defaultModel,
            @Value("${gemini.max-retries}") int maxRetries
    ) {
        this.geminiRestClient = geminiRestClient;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.maxRetries = maxRetries;
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

        log.info("Calling Gemini model={}", model);

        Map<String, Object> response =
                callGeminiWithRetry(uri, body);

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
    @SuppressWarnings("unchecked")
    private Map<String, Object> callGeminiWithRetry(
            String uri,
            Map<String, Object> body
    ) {

        for (int attempt = 1; attempt <= maxRetries; attempt++) {

            try {

                log.info(
                        "Gemini request attempt={}/{}",
                        attempt,
                        maxRetries
                );

                return geminiRestClient.post()
                        .uri(uri)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(Map.class);

            } catch (RestClientResponseException ex) {

                int statusCode =
                        ex.getStatusCode().value();

                // 429 = Rate Limit
                if (statusCode == 429) {

                    if (attempt == maxRetries) {
                        throw new ProviderRateLimitException(
                                "Gemini rate limit exceeded",
                                ex
                        );
                    }

                    sleepBeforeRetry(attempt);
                    continue;
                }

                // Retry lỗi server 5xx
                if (statusCode >= 500
                        && statusCode < 600) {

                    if (attempt == maxRetries) {
                        throw ex;
                    }

                    sleepBeforeRetry(attempt);
                    continue;
                }

                // 400, 401, 403... không retry
                throw ex;

            } catch (ResourceAccessException ex) {

                // Timeout / network error
                if (attempt == maxRetries) {
                    throw new ProviderTimeoutException(
                            "Gemini request timed out",
                            ex
                    );
                }

                sleepBeforeRetry(attempt);
            }
        }

        throw new IllegalStateException(
                "Gemini request failed"
        );
    }
    private void sleepBeforeRetry(int attempt) {

        try {

            long delayMs = 1000L * attempt;

            log.warn(
                    "Retrying Gemini after {} ms, attempt={}",
                    delayMs,
                    attempt
            );

            Thread.sleep(delayMs);

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Retry interrupted",
                    ex
            );
        }
    }
}