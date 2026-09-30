package com.baotrung.ai_gateway.dto.ai;

import jakarta.validation.constraints.NotBlank;

public class StructuredAIRequest {

    @NotBlank
    private String topic;

    public StructuredAIRequest() {
    }

    public StructuredAIRequest(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}