package com.baotrung.ai_gateway.dto.ai;

public class LLMResponse {

    private String content;
    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private String providerRequestId;

    public LLMResponse() {
    }

    public LLMResponse(
            String content,
            String model,
            Integer inputTokens,
            Integer outputTokens,
            String providerRequestId
    ) {
        this.content = content;
        this.model = model;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.providerRequestId = providerRequestId;
    }

    public String getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public String getProviderRequestId() {
        return providerRequestId;
    }
}