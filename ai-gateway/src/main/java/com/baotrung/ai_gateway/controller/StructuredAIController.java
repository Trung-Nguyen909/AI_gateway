package com.baotrung.ai_gateway.controller;

import com.baotrung.ai_gateway.dto.ai.StructuredAIRequest;
import com.baotrung.ai_gateway.dto.ai.StructuredAIResponse;
import com.baotrung.ai_gateway.service.StructuredAIService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class StructuredAIController {

    private final StructuredAIService structuredAIService;

    public StructuredAIController(
            StructuredAIService structuredAIService
    ) {
        this.structuredAIService =
                structuredAIService;
    }

    @PostMapping("/structured")
    public StructuredAIResponse generate(
            @Valid @RequestBody StructuredAIRequest request,
            JwtAuthenticationToken authentication
    ) {
        return structuredAIService.generate(
                request,
                authentication
        );
    }
}