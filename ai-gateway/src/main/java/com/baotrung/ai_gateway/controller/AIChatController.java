package com.baotrung.ai_gateway.controller;

import com.baotrung.ai_gateway.dto.ai.AIChatRequest;
import com.baotrung.ai_gateway.dto.ai.AIChatResponse;
import com.baotrung.ai_gateway.service.AIChatService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AIChatController {

    private final AIChatService aiChatService;

    public AIChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public AIChatResponse chat(
            @Valid @RequestBody AIChatRequest request,
            JwtAuthenticationToken authentication
    ) {
        return aiChatService.chat(
                request,
                authentication
        );
    }
}