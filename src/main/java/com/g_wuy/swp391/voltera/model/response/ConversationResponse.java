package com.g_wuy.swp391.voltera.model.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ConversationResponse {
    private Integer id;
    private String type;
    private Integer otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String lastMessage;
    private Instant lastMessageTime;
    private Integer unreadCount;
}