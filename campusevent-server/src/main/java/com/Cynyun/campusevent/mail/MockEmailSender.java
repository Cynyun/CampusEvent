package com.Cynyun.campusevent.mail;

import com.Cynyun.campusevent.entity.EmailLog;
import com.Cynyun.campusevent.mapper.EmailLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 模拟邮件：不接真实 SMTP，只把发送结果写入 email_log。
 * app.mock-email.fail=true 时写 FAILED 后抛异常，用于验证“邮件失败不影响报名”。
 */
@Component
@RequiredArgsConstructor
public class MockEmailSender implements EmailSender {

    private final EmailLogMapper emailLogMapper;

    @Value("${app.mock-email.fail:false}")
    private boolean fail;

    @Override
    public void send(EmailMessage message) {
        EmailLog log = new EmailLog();
        log.setRegistrationId(message.registrationId());
        log.setEventType(message.eventType().name());
        log.setRecipient(message.recipient());
        log.setSubject(message.subject());
        log.setContent(message.content());
        log.setCreatedAt(LocalDateTime.now());
        if (fail) {
            log.setStatus("FAILED");
            log.setErrorMessage("模拟邮件发送失败");
            emailLogMapper.insert(log);
            throw new IllegalStateException("模拟邮件发送失败");
        }
        log.setStatus("SENT");
        emailLogMapper.insert(log);
    }
}
