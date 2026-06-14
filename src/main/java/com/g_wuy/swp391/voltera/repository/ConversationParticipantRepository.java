package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Integer> {
    List<ConversationParticipant> findByConversationId(Integer conversationId);
    List<ConversationParticipant> findByUserId(Integer userId);
    Optional<ConversationParticipant> findByConversationIdAndUserId(Integer conversationId, Integer userId);
}