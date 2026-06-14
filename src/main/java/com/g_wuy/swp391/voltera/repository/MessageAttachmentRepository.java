package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Integer> {
    List<MessageAttachment> findByMessageId(Integer messageId);
}