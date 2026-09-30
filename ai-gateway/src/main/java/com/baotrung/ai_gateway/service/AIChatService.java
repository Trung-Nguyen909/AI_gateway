package com.baotrung.ai_gateway.service;

import com.baotrung.ai_gateway.dto.ai.AIChatRequest;
import com.baotrung.ai_gateway.dto.ai.AIChatResponse;
import com.baotrung.ai_gateway.dto.ai.LLMMessage;
import com.baotrung.ai_gateway.dto.ai.LLMRequest;
import com.baotrung.ai_gateway.dto.ai.LLMResponse;
import com.baotrung.ai_gateway.entity.AIRequest;
import com.baotrung.ai_gateway.entity.AIRequestStatus;
import com.baotrung.ai_gateway.entity.Conversation;
import com.baotrung.ai_gateway.entity.Message;
import com.baotrung.ai_gateway.entity.MessageRole;
import com.baotrung.ai_gateway.entity.User;
import com.baotrung.ai_gateway.provider.LLMProvider;
import com.baotrung.ai_gateway.repository.AIRequestRepository;
import com.baotrung.ai_gateway.repository.ConversationRepository;
import com.baotrung.ai_gateway.repository.MessageRepository;
import com.baotrung.ai_gateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import com.baotrung.ai_gateway.exception.ProviderRateLimitException;
import com.baotrung.ai_gateway.exception.ProviderTimeoutException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AIChatService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIRequestRepository aiRequestRepository;
    private final LLMProvider llmProvider;
    private final RateLimitService rateLimitService;

    public AIChatService(
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            AIRequestRepository aiRequestRepository,
            @Qualifier("geminiProvider") LLMProvider llmProvider,
            RateLimitService rateLimitService
    ) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiRequestRepository = aiRequestRepository;
        this.llmProvider = llmProvider;
        this.rateLimitService = rateLimitService;
    }

    public AIChatResponse chat(
            AIChatRequest request,
            JwtAuthenticationToken authentication
    ) {

        // 1. Lấy userId từ JWT
        String subject = authentication.getToken().getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("User ID not found in token");
        }

        Long userId = Long.valueOf(subject);

        // 2. Lấy User
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );
        rateLimitService.checkRateLimit(userId);

        // 3. Kiểm tra conversation thuộc user hiện tại
        Conversation conversation =
                conversationRepository
                        .findByIdAndUserId(
                                request.getConversationId(),
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Conversation not found"
                                )
                        );

        // 4. Xác định model
        String model = request.getModel();

        if (model == null || model.isBlank()) {
            model = "gemini-3.5-flash-lite";
        }

        // 5. Lấy lịch sử hội thoại
        List<Message> history =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversation.getId()
                        );

        List<LLMMessage> llmMessages = new ArrayList<>();

        for (Message message : history) {

            String role;

            if (message.getRole() == MessageRole.USER) {
                role = "user";
            } else {
                role = "assistant";
            }

            llmMessages.add(
                    new LLMMessage(
                            role,
                            message.getContent()
                    )
            );
        }

        // 6. Thêm câu hỏi hiện tại
        llmMessages.add(
                new LLMMessage(
                        "user",
                        request.getMessage()
                )
        );

        // 7. Tạo request gửi tới Gemini
        LLMRequest llmRequest =
                new LLMRequest(
                        model,
                        llmMessages
                );

        // 8. Tạo requestId nội bộ
        String requestId = UUID.randomUUID().toString();

        // 9. Lưu USER message
        Message userMessage = new Message();

        userMessage.setConversation(conversation);
        userMessage.setRole(MessageRole.USER);
        userMessage.setContent(request.getMessage());

        messageRepository.save(userMessage);

        // 10. Gọi Gemini và đo latency
        long startTime = System.currentTimeMillis();

        try {

            LLMResponse llmResponse =
                    llmProvider.chat(llmRequest);

            long latencyMs =
                    System.currentTimeMillis() - startTime;

            // 11. Lưu ASSISTANT message
            Message assistantMessage = new Message();

            assistantMessage.setConversation(conversation);
            assistantMessage.setRole(MessageRole.ASSISTANT);
            assistantMessage.setContent(
                    llmResponse.getContent()
            );

            messageRepository.save(assistantMessage);

            // 12. Lưu AI request thành công
            AIRequest aiRequest = new AIRequest();

            aiRequest.setUser(user);
            aiRequest.setConversation(conversation);
            aiRequest.setRequestId(requestId);
            aiRequest.setModel(llmResponse.getModel());
            aiRequest.setLatencyMs(latencyMs);
            aiRequest.setInputTokens(
                    llmResponse.getInputTokens()
            );
            aiRequest.setOutputTokens(
                    llmResponse.getOutputTokens()
            );
            aiRequest.setStatus(AIRequestStatus.SUCCESS);

            aiRequestRepository.save(aiRequest);

            // 13. Trả response
            return new AIChatResponse(
                    requestId,
                    conversation.getId(),
                    llmResponse.getModel(),
                    llmResponse.getContent(),
                    llmResponse.getInputTokens(),
                    llmResponse.getOutputTokens(),
                    latencyMs
            );

        } catch (Exception ex) {

            long latencyMs =
                    System.currentTimeMillis() - startTime;

            AIRequestStatus status;

            if (ex instanceof ProviderTimeoutException) {

                status = AIRequestStatus.TIMEOUT;

            } else if (ex instanceof ProviderRateLimitException) {

                status = AIRequestStatus.RATE_LIMITED;

            } else {

                status = AIRequestStatus.ERROR;
            }

            AIRequest aiRequest = new AIRequest();

            aiRequest.setUser(user);
            aiRequest.setConversation(conversation);
            aiRequest.setRequestId(requestId);
            aiRequest.setModel(model);
            aiRequest.setLatencyMs(latencyMs);
            aiRequest.setStatus(status);
            aiRequest.setErrorCode(
                    ex.getClass().getSimpleName()
            );

            aiRequestRepository.save(aiRequest);

            throw ex;
        }
    }
}