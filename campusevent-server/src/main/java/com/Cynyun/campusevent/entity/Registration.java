package com.Cynyun.campusevent.entity;

import com.Cynyun.campusevent.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Registration {
    private Long id;
    private String studentId;
    private Long activityId;
    private RegistrationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == RegistrationStatus.PENDING || status == RegistrationStatus.APPROVED;
    }
}