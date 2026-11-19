package com.Cynyun.campusevent.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private String studentId;
    private Long activityId;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
}