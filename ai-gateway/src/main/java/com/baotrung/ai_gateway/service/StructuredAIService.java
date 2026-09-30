package com.baotrung.ai_gateway.service;

import com.baotrung.ai_gateway.dto.ai.LLMMessage;
import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;
import com.baotrung.ai_gateway.dto.ai.StructuredAIRequest;
import com.baotrung.ai_gateway.dto.ai.StructuredAIResponse;
import com.baotrung.ai_gateway.provider.LLMProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.baotrung.ai_gateway.entity.AIRequest;
import com.baotrung.ai_gateway.entity.AIRequestStatus;
import com.baotrung.ai_gateway.entity.User;
import com.baotrung.ai_gateway.exception.ProviderRateLimitException;
import com.baotrung.ai_gateway.exception.ProviderTimeoutException;
import com.baotrung.ai_gateway.repository.AIRequestRepository;
import com.baotrung.ai_gateway.repository.UserRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

import java.util.List;

@Service
public class StructuredAIService {

    private final LLMProvider llmProvider;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AIRequestRepository aiRequestRepository;
    private final RateLimitService rateLimitService;

    public StructuredAIService(
            @Qualifier("geminiProvider") LLMProvider llmProvider,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            AIRequestRepository aiRequestRepository,
            RateLimitService rateLimitService
    ) {
        this.llmProvider = llmProvider;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.aiRequestRepository = aiRequestRepository;
        this.rateLimitService = rateLimitService;
    }

    public StructuredAIResponse generate(
            StructuredAIRequest request,
            JwtAuthenticationToken authentication
    ) {

        String subject = authentication.getToken().getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "User ID not found in token"
            );
        }

        Long userId = Long.valueOf(subject);

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User not found"
                        )
                );
        rateLimitService.checkRateLimit(userId);

        String model = "gemini-3.5-flash-lite";
        String requestId = UUID.randomUUID().toString();

        String prompt = """
            Explain the following topic: %s

            Return ONLY valid JSON.
            Do not use Markdown.
            Do not use ```json.

            Use exactly this structure:
            {
              "title": "string",
              "summary": "string",
              "keyPoints": [
                "string",
                "string",
                "string"
              ]
            }
            """.formatted(request.getTopic());

        LLMRequest llmRequest = new LLMRequest(
                model,
                List.of(
                        new LLMMessage(
                                "user",
                                prompt
                        )
                )
        );

        long startTime = System.currentTimeMillis();

        try {

            LLMResponse response =
                    llmProvider.chat(llmRequest);

            StructuredAIResponse structuredResponse;

            try {

                structuredResponse = objectMapper.readValue(
                        response.getContent(),
                        StructuredAIResponse.class
                );

            } catch (JacksonException ex) {

                throw new IllegalStateException(
                        "AI returned invalid structured JSON",
                        ex
                );
            }

            long latencyMs =
                    System.currentTimeMillis() - startTime;

            AIRequest aiRequest = new AIRequest();

            aiRequest.setUser(user);
            aiRequest.setRequestId(requestId);
            aiRequest.setModel(response.getModel());
            aiRequest.setLatencyMs(latencyMs);
            aiRequest.setInputTokens(
                    response.getInputTokens()
            );
            aiRequest.setOutputTokens(
                    response.getOutputTokens()
            );
            aiRequest.setStatus(
                    AIRequestStatus.SUCCESS
            );

            aiRequestRepository.save(aiRequest);

            return structuredResponse;

        } catch (Exception ex) {

            long latencyMs =
                    System.currentTimeMillis() - startTime;

            AIRequestStatus status;

            if (ex instanceof ProviderTimeoutException) {

                status = AIRequestStatus.TIMEOUT;

            } else if (
                    ex instanceof ProviderRateLimitException
            ) {

                status = AIRequestStatus.RATE_LIMITED;

            } else {

                status = AIRequestStatus.ERROR;
            }

            AIRequest aiRequest = new AIRequest();

            aiRequest.setUser(user);
            aiRequest.setRequestId(requestId);
            aiRequest.setModel(model);
            aiRequest.setLatencyMs(latencyMs);
            aiRequest.setStatus(status);
            aiRequest.setErrorCode(
                    ex.getClass().getSimpleName()
            );

            aiRequestRepository.save(aiRequest);

            throw ex;
        }
    }
}