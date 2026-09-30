package com.baotrung.ai_gateway.service;

import com.baotrung.ai_gateway.dto.usage.UsageResponse;
import com.baotrung.ai_gateway.entity.AIRequest;
import com.baotrung.ai_gateway.entity.AIRequestStatus;
import com.baotrung.ai_gateway.repository.AIRequestRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsageService {

    private final AIRequestRepository aiRequestRepository;

    public UsageService(
            AIRequestRepository aiRequestRepository
    ) {
        this.aiRequestRepository = aiRequestRepository;
    }

    public UsageResponse getUsage(
            JwtAuthenticationToken authentication
    ) {

        String subject =
                authentication.getToken().getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException(
                    "User ID not found in token"
            );
        }

        Long userId = Long.valueOf(subject);

        List<AIRequest> requests =
                aiRequestRepository
                        .findByUserIdOrderByTimestampDesc(userId);

        long totalRequests = requests.size();

        long totalInputTokens = 0;
        long totalOutputTokens = 0;
        long totalLatency = 0;
        long requestsWithLatency = 0;
        long failedRequests = 0;

        for (AIRequest request : requests) {

            if (request.getInputTokens() != null) {
                totalInputTokens +=
                        request.getInputTokens();
            }

            if (request.getOutputTokens() != null) {
                totalOutputTokens +=
                        request.getOutputTokens();
            }

            if (request.getLatencyMs() != null) {
                totalLatency +=
                        request.getLatencyMs();

                requestsWithLatency++;
            }

            if (request.getStatus()
                    != AIRequestStatus.SUCCESS) {

                failedRequests++;
            }
        }

        long totalTokens =
                totalInputTokens + totalOutputTokens;

        double averageLatencyMs =
                requestsWithLatency == 0
                        ? 0
                        : (double) totalLatency
                          / requestsWithLatency;

        double errorRate =
                totalRequests == 0
                        ? 0
                        : ((double) failedRequests
                           / totalRequests) * 100;

        return new UsageResponse(
                totalRequests,
                totalInputTokens,
                totalOutputTokens,
                totalTokens,
                averageLatencyMs,
                errorRate
        );
    }
}