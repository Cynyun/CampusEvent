package com.Cynyun.campusevent.mail;

import com.Cynyun.campusevent.enums.RegistrationEventType;

/**
 * 一次模拟邮件发送的完整内容，便于日志落库与测试断言。
 */
public record EmailMessage(Long registrationId, RegistrationEventType eventType,
                           String recipient, String subject, String content) {
}
