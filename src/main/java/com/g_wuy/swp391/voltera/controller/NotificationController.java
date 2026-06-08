package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.entity.Notification;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.model.response.NotificationResponse;
import com.g_wuy.swp391.voltera.repository.NotificationRepository;
import com.g_wuy.swp391.voltera.repository.UserRepository;
import com.g_wuy.swp391.voltera.service.JwtService;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationRepository notificationRepository;

    UserRepository userRepository;

    JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {

        User currentUser = getCurrentUser(authHeader);

        List<NotificationResponse> notifications = notificationRepository
                .findByUseridOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .createdAt(n.getCreatedAt())
                        .readStatus(n.getReadStatus())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Integer notificationId,
            @RequestHeader("Authorization") String authHeader) {

        User currentUser = getCurrentUser(authHeader);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserid().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot update someone else's notification.");
        }

        notification.setReadStatus(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification marked as read.");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable Integer notificationId,
            @RequestHeader("Authorization") String authHeader) {

        User currentUser = getCurrentUser(authHeader);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserid().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot delete someone else's notification.");
        }

        notificationRepository.delete(notification);
        return ResponseEntity.ok("Notification deleted successfully.");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {

        User currentUser = getCurrentUser(authHeader);

        List<Notification> notifications = notificationRepository
                .findByUseridOrderByCreatedAtDesc(currentUser);

        notifications.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok("All notifications marked as read.");
    }

    private User getCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Missing or invalid Authorization header");

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        return userRepository.findUserByUsername(username);
    }
}
