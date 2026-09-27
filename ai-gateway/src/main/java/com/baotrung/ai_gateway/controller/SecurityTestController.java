package com.baotrung.ai_gateway.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SecurityTestController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {

        return Map.of(
                "authenticated", authentication.isAuthenticated(),
                "username", authentication.getName()
        );
    }
}