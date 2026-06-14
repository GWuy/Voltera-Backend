package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.request.ChatMessageRequest;
import com.g_wuy.swp391.voltera.model.response.ChatMessageResponse;
import com.g_wuy.swp391.voltera.model.response.ConversationResponse;
import com.g_wuy.swp391.voltera.service.ChatService;
import com.g_wuy.swp391.voltera.service.JwtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    ChatService chatService;
    SimpMessagingTemplate messagingTemplate;
    JwtService jwtService;

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

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {

        try {
            log.debug("========== CHAT SEND ==========");
            log.debug("Payload: {}", request);

            String username = null;

            if (principal != null) {
                username = principal.getName();
                log.debug("Principal username: {}", username);
            }

            if (username == null) {
                String authHeader = headerAccessor.getFirstNativeHeader("Authorization");
                log.debug("Fallback Authorization header: {}", authHeader);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("No authentication found in STOMP SEND");
                    return;
                }
                String token = authHeader.substring(7);
                username = jwtService.extractUsername(token);
            }

            ChatMessageResponse response = chatService.sendMessageByUsername(username, request);

            log.debug("Sending to /user/{}/queue/messages", request.getReceiverId());
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(request.getReceiverId()),
                    "/queue/messages",
                    response
            );

            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/messages",
                    response
            );

        } catch (Exception e) {
            log.error("Error processing chat.send", e);
        }
    }
}
