package com.baotrung.ai_gateway.repository;

import com.baotrung.ai_gateway.entity.AIRequest;
import com.baotrung.ai_gateway.entity.AIRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIRequestRepository extends JpaRepository<AIRequest, Long> {

    List<AIRequest> findByUserIdOrderByTimestampDesc(Long userId);

    long countByUserId(Long userId);

    long countByStatus(AIRequestStatus status);
}