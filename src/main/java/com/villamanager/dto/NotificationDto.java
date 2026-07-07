package com.villamanager.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private Long villaId;
    private Long userId;
    private String title;
    private String body;
    private String type;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
