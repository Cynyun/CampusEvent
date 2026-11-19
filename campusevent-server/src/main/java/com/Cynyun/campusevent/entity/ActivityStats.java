package com.Cynyun.campusevent.entity;

import java.time.LocalDateTime;

public record ActivityStats(
        Long activityId,
        String title,
        LocalDateTime startAt,
        Integer capacity,
        int approvedCount,
        int pendingCount,
        int remaining,
        boolean open
) {
    public static ActivityStats from(Activity activity) {
        return new ActivityStats(
                activity.getId(),
                activity.getTitle(),
                activity.getStartAt(),
                activity.getCapacity(),
                activity.getApprovedCount(),
                activity.getPendingCount(),
                activity.getRemaining(),
                activity.isOpen()
        );
    }
}