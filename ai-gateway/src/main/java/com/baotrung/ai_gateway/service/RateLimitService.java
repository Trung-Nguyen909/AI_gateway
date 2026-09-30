package com.baotrung.ai_gateway.service;

import com.baotrung.ai_gateway.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<Long, UserRateLimit> userLimits =
            new ConcurrentHashMap<>();

    private final int maxRequests;
    private final long windowSeconds;

    public RateLimitService(
            @Value("${rate-limit.max-requests:10}") int maxRequests,
            @Value("${rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public void checkRateLimit(Long userId) {

        long now = Instant.now().getEpochSecond();

        UserRateLimit rateLimit = userLimits.compute(
                userId,
                (id, current) -> {

                    if (current == null
                            || now - current.windowStart >= windowSeconds) {

                        return new UserRateLimit(now, 1);
                    }

                    if (current.requestCount >= maxRequests) {
                        return current;
                    }

                    current.requestCount++;
                    return current;
                }
        );

        if (rateLimit.requestCount >= maxRequests
                && now - rateLimit.windowStart < windowSeconds) {

            /*
             * Nếu request hiện tại chính là request thứ maxRequests
             * thì vẫn phải cho qua.
             */
            if (rateLimit.requestCount == maxRequests) {

                synchronized (rateLimit) {

                    if (!rateLimit.limitReachedOnce) {
                        rateLimit.limitReachedOnce = true;
                        return;
                    }
                }
            }

            throw new RateLimitExceededException(
                    "Too many AI requests. Please try again later."
            );
        }
    }

    private static class UserRateLimit {

        private final long windowStart;
        private int requestCount;
        private boolean limitReachedOnce;

        private UserRateLimit(
                long windowStart,
                int requestCount
        ) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
            this.limitReachedOnce = false;
        }
    }
}