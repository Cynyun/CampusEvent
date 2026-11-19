package com.Cynyun.campusevent.event;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEventPublisher.class);

    private final List<NotificationObserver> observers;

    public void publish(RegistrationEvent event) {
        for (NotificationObserver observer : observers) {
            try {
                observer.onEvent(event);
            } catch (Exception e) {
                log.warn("Notification failed for registration {}, type {}", event.getRegistrationId(), event.getType(), e);
            }
        }
    }
}