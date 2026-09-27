package com.baotrung.ai_gateway.provider;

import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;

public interface LLMProvider {

    String getProviderName();

    boolean supportsModel(String model);

    LLMResponse chat(LLMRequest request);
}