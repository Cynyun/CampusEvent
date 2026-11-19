package com.Cynyun.campusevent.entity;

import com.Cynyun.campusevent.enums.PolicyType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    private String title;
    private String category;
    private LocalDateTime startAt;
    private String location;
    private Integer capacity;
    private PolicyType policyType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer approvedCount;
    private Integer pendingCount;
    private String myRegistrationStatus;
    private Long myRegistrationId;

    public int getApprovedCount() {
        return approvedCount == null ? 0 : approvedCount;
    }

    public int getPendingCount() {
        return pendingCount == null ? 0 : pendingCount;
    }

    public int getRemaining() {
        int max = capacity == null ? 0 : capacity;
        return Math.max(0, max - getApprovedCount());
    }

    public boolean isOpen() {
        return deletedAt == null && startAt != null && startAt.isAfter(LocalDateTime.now());
    }

    public boolean isHasCapacity() {
        int max = capacity == null ? 0 : capacity;
        return getApprovedCount() < max;
    }
}