package com.baotrung.ai_gateway.dto.ai;

import java.util.List;

public class StructuredAIResponse {

    private String title;
    private String summary;
    private List<String> keyPoints;

    public StructuredAIResponse() {
    }

    public StructuredAIResponse(
            String title,
            String summary,
            List<String> keyPoints
    ) {
        this.title = title;
        this.summary = summary;
        this.keyPoints = keyPoints;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyPoints() {
        return keyPoints;
    }

    public void setKeyPoints(List<String> keyPoints) {
        this.keyPoints = keyPoints;
    }
}