package com.baotrung.ai_gateway.controller;

import com.baotrung.ai_gateway.dto.conversation.ConversationResponse;
import com.baotrung.ai_gateway.dto.conversation.CreateConversationRequest;
import com.baotrung.ai_gateway.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService
    ) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> create(
            @Valid @RequestBody CreateConversationRequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        ConversationResponse response =
                conversationService.create(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getAll(
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                conversationService.getAll(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                conversationService.getById(userId, id)
        );
    }
}