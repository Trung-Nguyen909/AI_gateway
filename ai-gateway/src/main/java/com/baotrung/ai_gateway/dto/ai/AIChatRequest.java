package com.baotrung.ai_gateway.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AIChatRequest {

    @NotNull
    private Long conversationId;

    @NotBlank
    private String message;

    private String model;

    public AIChatRequest() {
    }

    public AIChatRequest(
            Long conversationId,
            String message,
            String model
    ) {
        this.conversationId = conversationId;
        this.message = message;
        this.model = model;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}