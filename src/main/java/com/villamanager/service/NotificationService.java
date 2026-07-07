package com.villamanager.service;

import com.villamanager.dto.NotificationDto;
import com.villamanager.entity.Notification;
import com.villamanager.entity.User;
import com.villamanager.repository.NotificationRepository;
import com.villamanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    // Send notification to all users of a villa
    public void notifyVilla(Long villaId, String title, String body, String type, Long referenceId) {
        List<User> villaUsers = userRepository.findAll().stream()
                .filter(u -> villaId.equals(u.getVillaId()) && Boolean.TRUE.equals(u.getIsActive()))
                .collect(Collectors.toList());

        for (User user : villaUsers) {
            // Save to DB
            Notification notification = new Notification();
            notification.setVillaId(villaId);
            notification.setUserId(user.getId());
            notification.setTitle(title);
            notification.setBody(body);
            notification.setType(type);
            notification.setReferenceId(referenceId);
            notification.setIsRead(false);
            notificationRepository.save(notification);

            // Send push if token exists
            if (user.getPushToken() != null && !user.getPushToken().isBlank()) {
                sendExpoPush(user.getPushToken(), title, body, type);
            }
        }
    }

    // Get notifications for current user
    public List<NotificationDto> getMyNotifications() {
        User user = accessControlService.currentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // Get unread count for current user
    public long getUnreadCount() {
        User user = accessControlService.currentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    // Mark all as read
    public void markAllRead() {
        User user = accessControlService.currentUser();
        notificationRepository.markAllReadByUserId(user.getId());
    }

    // Mark single as read
    public void markRead(Long id) {
        notificationRepository.markReadById(id);
    }

    // Send push via Expo Push API
    private void sendExpoPush(String pushToken, String title, String body, String type) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", pushToken);
            payload.put("title", title);
            payload.put("body", body);
            payload.put("sound", "default");
            payload.put("data", Map.of("type", type));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity("https://exp.host/--/api/v2/push/send", request, String.class);
        } catch (Exception e) {
            log.warn("Failed to send push notification to token {}: {}", pushToken, e.getMessage());
        }
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setVillaId(n.getVillaId());
        dto.setUserId(n.getUserId());
        dto.setTitle(n.getTitle());
        dto.setBody(n.getBody());
        dto.setType(n.getType());
        dto.setReferenceId(n.getReferenceId());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
