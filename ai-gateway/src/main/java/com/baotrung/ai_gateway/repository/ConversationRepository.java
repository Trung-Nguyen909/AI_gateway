package com.baotrung.ai_gateway.repository;

import com.baotrung.ai_gateway.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
}