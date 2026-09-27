package com.baotrung.ai_gateway.service;

import com.baotrung.ai_gateway.dto.conversation.ConversationResponse;
import com.baotrung.ai_gateway.dto.conversation.CreateConversationRequest;
import com.baotrung.ai_gateway.entity.Conversation;
import com.baotrung.ai_gateway.entity.User;
import com.baotrung.ai_gateway.repository.ConversationRepository;
import com.baotrung.ai_gateway.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConversationResponse create(
            Long userId,
            CreateConversationRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(request.getTitle().trim());

        Conversation saved = conversationRepository.save(conversation);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getAll(Long userId) {
        return conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getById(
            Long userId,
            Long conversationId
    ) {
        Conversation conversation = conversationRepository
                .findByIdAndUserId(conversationId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation not found")
                );

        return toResponse(conversation);
    }

    private ConversationResponse toResponse(
            Conversation conversation
    ) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}