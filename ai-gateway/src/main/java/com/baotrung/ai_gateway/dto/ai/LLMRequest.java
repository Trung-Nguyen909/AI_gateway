package com.baotrung.ai_gateway.dto.ai;

import java.util.List;

public class LLMRequest {

    private String model;
    private List<LLMMessage> messages;

    public LLMRequest() {
    }

    public LLMRequest(String model, List<LLMMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<LLMMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LLMMessage> messages) {
        this.messages = messages;
    }
}