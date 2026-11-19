package com.Cynyun.campusevent.event;

import com.Cynyun.campusevent.enums.RegistrationEventType;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RegistrationEvent {
    private final RegistrationEventType type;
    private final RegistrationStatus status;
    private final Long registrationId;
    private final String studentId;
    private final Long activityId;
    private final LocalDateTime occurredAt;
}