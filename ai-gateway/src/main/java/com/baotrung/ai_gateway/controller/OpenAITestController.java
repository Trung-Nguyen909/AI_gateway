package com.baotrung.ai_gateway.controller;

import com.baotrung.ai_gateway.dto.ai.LLMMessage;
import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;
import com.baotrung.ai_gateway.provider.LLMProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test-ai")
public class OpenAITestController {

    private final LLMProvider llmProvider;

    public OpenAITestController(
            @Qualifier("geminiProvider") LLMProvider llmProvider
    ) {
        this.llmProvider = llmProvider;
    }

    @PostMapping
    public Map<String, Object> testAI(
            @RequestBody Map<String, String> body
    ) {

        String message = body.get("message");

        LLMRequest request = new LLMRequest(
                "gemini-3.5-flash-lite",
                List.of(
                        new LLMMessage("user", message)
                )
        );

        LLMResponse response = llmProvider.chat(request);

        return Map.of(
                "provider", llmProvider.getProviderName(),
                "model", response.getModel(),
                "content", response.getContent(),
                "inputTokens", response.getInputTokens(),
                "outputTokens", response.getOutputTokens(),
                "providerRequestId", response.getProviderRequestId()
        );
    }
}