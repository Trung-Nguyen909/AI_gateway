package com.baotrung.ai_gateway.dto.usage;

public class UsageResponse {

    private long totalRequests;
    private long totalInputTokens;
    private long totalOutputTokens;
    private long totalTokens;
    private double averageLatencyMs;
    private double errorRate;

    public UsageResponse() {
    }

    public UsageResponse(
            long totalRequests,
            long totalInputTokens,
            long totalOutputTokens,
            long totalTokens,
            double averageLatencyMs,
            double errorRate
    ) {
        this.totalRequests = totalRequests;
        this.totalInputTokens = totalInputTokens;
        this.totalOutputTokens = totalOutputTokens;
        this.totalTokens = totalTokens;
        this.averageLatencyMs = averageLatencyMs;
        this.errorRate = errorRate;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getTotalInputTokens() {
        return totalInputTokens;
    }

    public void setTotalInputTokens(long totalInputTokens) {
        this.totalInputTokens = totalInputTokens;
    }

    public long getTotalOutputTokens() {
        return totalOutputTokens;
    }

    public void setTotalOutputTokens(long totalOutputTokens) {
        this.totalOutputTokens = totalOutputTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }
}