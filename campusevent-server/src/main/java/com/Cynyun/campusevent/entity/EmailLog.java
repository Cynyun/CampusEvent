package com.Cynyun.campusevent.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailLog {
    private Long id;
    private Long registrationId;
    private String eventType;
    private String recipient;
    private String subject;
    private String content;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
