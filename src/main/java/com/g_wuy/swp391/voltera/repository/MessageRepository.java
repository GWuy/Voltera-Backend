package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Integer conversationId, Pageable pageable);
    List<Message> findByConversationId(Integer conversationId);
}