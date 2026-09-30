package com.baotrung.ai_gateway.controller;

import com.baotrung.ai_gateway.dto.usage.UsageResponse;
import com.baotrung.ai_gateway.service.UsageService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(
            UsageService usageService
    ) {
        this.usageService = usageService;
    }

    @GetMapping
    public UsageResponse getUsage(
            JwtAuthenticationToken authentication
    ) {
        return usageService.getUsage(authentication);
    }
}