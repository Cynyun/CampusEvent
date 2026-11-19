package com.Cynyun.campusevent.entity;

import com.Cynyun.campusevent.enums.PolicyType;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationView {
    private Long id;
    private String studentId;
    private String studentName;
    private Long activityId;
    private String activityTitle;
    private LocalDateTime activityStartAt;
    private String activityLocation;
    private PolicyType policyType;
    private RegistrationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}