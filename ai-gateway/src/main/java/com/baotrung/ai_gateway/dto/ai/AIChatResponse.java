package com.baotrung.ai_gateway.dto.ai;

public class AIChatResponse {

    private String requestId;
    private Long conversationId;
    private String model;
    private String content;
    private Integer inputTokens;
    private Integer outputTokens;
    private Long latencyMs;

    public AIChatResponse() {
    }

    public AIChatResponse(
            String requestId,
            Long conversationId,
            String model,
            String content,
            Integer inputTokens,
            Integer outputTokens,
            Long latencyMs
    ) {
        this.requestId = requestId;
        this.conversationId = conversationId;
        this.model = model;
        this.content = content;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.latencyMs = latencyMs;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }
}