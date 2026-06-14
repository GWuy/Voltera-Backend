package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.*;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.model.request.ChatMessageRequest;
import com.g_wuy.swp391.voltera.model.response.ChatMessageResponse;
import com.g_wuy.swp391.voltera.model.response.ConversationResponse;
import com.g_wuy.swp391.voltera.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {

    MessageRepository messageRepository;
    MessageAttachmentRepository attachmentRepository;
    ConversationRepository conversationRepository;
    ConversationParticipantRepository participantRepository;
    UserRepository userRepository;
    UserService userService;
    JwtService jwtService;

    @Transactional
    public ChatMessageResponse sendMessage(String token, ChatMessageRequest request) {
        String username = jwtService.extractUsername(token.substring(7));
        User sender = userService.findUserByUsername(username);
        if (sender == null) {
            throw new BusinessException("User not found");
        }

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new BusinessException("Receiver not found"));

        Conversation conversation = getOrCreateConversation(sender.getId(), receiver.getId());

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : "TEXT");
        message.setCreatedAt(Instant.now());
        message.setIsDeleted(false);

        Message savedMessage = messageRepository.save(message);

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (ChatMessageRequest.AttachmentRequest attachmentReq : request.getAttachments()) {
                MessageAttachment attachment = new MessageAttachment();
                attachment.setMessage(savedMessage);
                attachment.setFileUrl(attachmentReq.getFileUrl());
                attachment.setFileType(attachmentReq.getFileType());
                attachment.setCreatedAt(Instant.now());
                attachmentRepository.save(attachment);
            }
        }

        return toMessageResponse(savedMessage);
    }

    @Transactional
    public Page<ChatMessageResponse> getMessageHistory(String token, Integer receiverId, int page, int size) {
        String username = jwtService.extractUsername(token.substring(7));
        User sender = userService.findUserByUsername(username);
        if (sender == null) {
            throw new BusinessException("User not found");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("Receiver not found"));

        Conversation conversation = getOrCreateConversation(sender.getId(), receiver.getId());

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable);

        return messages.map(this::toMessageResponse);
    }

    @Transactional
    public void markMessagesAsRead(String token, Integer senderId) {
        String username = jwtService.extractUsername(token.substring(7));
        User receiver = userService.findUserByUsername(username);
        if (receiver == null) {
            throw new BusinessException("User not found");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException("Sender not found"));

        Conversation conversation = getOrCreateConversation(sender.getId(), receiver.getId());

        ConversationParticipant participant = participantRepository
                .findByConversationIdAndUserId(conversation.getId(), receiver.getId())
                .orElseThrow(() -> new BusinessException("Participant not found"));

        List<Message> messages = messageRepository.findByConversationId(conversation.getId());
        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            participant.setLastReadMessageId(lastMessage.getId());
            participantRepository.save(participant);
        }
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(String token) {
        String username = jwtService.extractUsername(token.substring(7));
        User user = userService.findUserByUsername(username);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        List<ConversationParticipant> participants = participantRepository.findByUserId(user.getId());

        return participants.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .map(p -> buildConversationResponse(p, user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    protected Conversation getOrCreateConversation(Integer userId1, Integer userId2) {
        List<ConversationParticipant> participants1 = participantRepository.findByUserId(userId1);

        for (ConversationParticipant p1 : participants1) {
            List<ConversationParticipant> participants2 = participantRepository
                    .findByConversationId(p1.getConversation().getId());

            for (ConversationParticipant p2 : participants2) {
                if (p2.getUser().getId().equals(userId2)) {
                    return p1.getConversation();
                }
            }
        }

        Conversation conversation = new Conversation();
        conversation.setType("PRIVATE");
        conversation.setCreatedAt(java.time.OffsetDateTime.now());
        conversation.setUpdatedAt(java.time.OffsetDateTime.now());
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant participant1 = new ConversationParticipant();
        participant1.setConversation(savedConversation);
        participant1.setUser(userRepository.findById(userId1).orElseThrow());
        participant1.setJoinedAt(Instant.now());
        participant1.setIsActive(true);
        participantRepository.save(participant1);

        ConversationParticipant participant2 = new ConversationParticipant();
        participant2.setConversation(savedConversation);
        participant2.setUser(userRepository.findById(userId2).orElseThrow());
        participant2.setJoinedAt(Instant.now());
        participant2.setIsActive(true);
        participantRepository.save(participant2);

        return savedConversation;
    }

    private ChatMessageResponse toMessageResponse(Message message) {
        List<ChatMessageResponse.AttachmentResponse> attachments = attachmentRepository
                .findByMessageId(message.getId())
                .stream()
                .map(a -> ChatMessageResponse.AttachmentResponse.builder()
                        .id(a.getId())
                        .fileUrl(a.getFileUrl())
                        .fileType(a.getFileType())
                        .build())
                .collect(Collectors.toList());

        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullname())
                .senderAvatar(message.getSender().getAvatar())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .attachments(attachments)
                .build();
    }

    private ConversationResponse buildConversationResponse(ConversationParticipant participant, Integer currentUserId) {
        Conversation conversation = participant.getConversation();
        List<ConversationParticipant> allParticipants = participantRepository
                .findByConversationId(conversation.getId());

        User otherUser = allParticipants.stream()
                .map(ConversationParticipant::getUser)
                .filter(u -> !u.getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        List<Message> messages = messageRepository.findByConversationId(conversation.getId());
        Message lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        int unreadCount = (int) messages.stream()
                .filter(m -> !m.getSender().getId().equals(currentUserId))
                .filter(m -> participant.getLastReadMessageId() == null || 
                        m.getId() > participant.getLastReadMessageId())
                .count();

        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .otherUserId(otherUser != null ? otherUser.getId() : null)
                .otherUserName(otherUser != null ? otherUser.getFullname() : "Unknown")
                .otherUserAvatar(otherUser != null ? otherUser.getAvatar() : null)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .unreadCount(unreadCount)
                .build();
    }
}
