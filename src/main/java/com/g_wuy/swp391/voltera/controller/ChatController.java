package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.request.ChatMessageRequest;
import com.g_wuy.swp391.voltera.model.response.ChatMessageResponse;
import com.g_wuy.swp391.voltera.model.response.ConversationResponse;
import com.g_wuy.swp391.voltera.service.ChatService;
import com.g_wuy.swp391.voltera.service.JwtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    ChatService chatService;
    SimpMessagingTemplate messagingTemplate;
    JwtService jwtService;

    // REST Endpoints
    
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(chatService.getUserConversations(auth));
    }

    @GetMapping("/messages/{receiverId}")
    public ResponseEntity<Page<ChatMessageResponse>> getMessageHistory(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer receiverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessageHistory(auth, receiverId, page, size));
    }

    @PutMapping("/messages/{senderId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @RequestHeader("Authorization") String auth,
            @PathVariable Integer senderId) {
        chatService.markMessagesAsRead(auth, senderId);
        return ResponseEntity.ok().build();
    }

    // WebSocket Endpoints

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token == null) {
            return;
        }

        ChatMessageResponse response = chatService.sendMessage(token, request);

        // Send to receiver's queue
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/messages",
                response
        );
        
        // Send back to sender's queue for confirmation and UI update
        String senderUsername = jwtService.extractUsername(token.substring(7));
        messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/messages",
                response
        );
    }
}
