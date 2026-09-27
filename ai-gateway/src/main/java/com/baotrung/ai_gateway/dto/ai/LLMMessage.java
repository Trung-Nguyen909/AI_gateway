package com.baotrung.ai_gateway.dto.ai;

public class LLMMessage {

    private String role;
    private String content;

    public LLMMessage() {
    }

    public LLMMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}