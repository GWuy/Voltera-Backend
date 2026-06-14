package com.g_wuy.swp391.voltera.model.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ChatMessageResponse {
    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String messageType;
    private Instant createdAt;
    private List<AttachmentResponse> attachments;

    @Data
    @Builder
    public static class AttachmentResponse {
        private Integer id;
        private String fileUrl;
        private String fileType;
    }
}