package com.g_wuy.swp391.voltera.model.request;

import lombok.Data;
import java.util.List;

@Data
public class ChatMessageRequest {
    private Integer receiverId;
    private String content;
    private String messageType; // TEXT, IMAGE, FILE
    private List<AttachmentRequest> attachments;

    @Data
    public static class AttachmentRequest {
        private String fileUrl;
        private String fileType;
        private String fileName;
        private Long fileSize;
    }
}