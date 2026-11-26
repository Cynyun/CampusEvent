package com.Cynyun.campusevent.mail;

import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.enums.RegistrationEventType;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import com.Cynyun.campusevent.event.NotificationObserver;
import com.Cynyun.campusevent.event.RegistrationEvent;
import com.Cynyun.campusevent.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 邮件通知观察者：只负责把报名事件翻译成邮件内容。
 * 不修改报名状态，也不参与事务；失败由 Publisher 捕获，不回滚报名。
 */
@Component
@RequiredArgsConstructor
public class EmailNotificationObserver implements NotificationObserver {

    private final ActivityMapper activityMapper;
    private final EmailSender emailSender;

    @Override
    public void onEvent(RegistrationEvent event) {
        if (event.getType() == RegistrationEventType.SUBMITTED
                && event.getStatus() != RegistrationStatus.APPROVED) {
            return;
        }
        if (event.getType() != RegistrationEventType.SUBMITTED
                && event.getType() != RegistrationEventType.APPROVED
                && event.getType() != RegistrationEventType.REJECTED) {
            return;
        }

        Activity activity = activityMapper.findById(event.getActivityId());
        String title = activity == null ? "活动 #" + event.getActivityId() : activity.getTitle();
        String recipient = event.getStudentId() + "@campus.example";

        String subject;
        String content;
        switch (event.getType()) {
            case SUBMITTED -> {
                subject = "报名成功：《" + title + "》";
                content = "你已成功报名《" + title + "》，请按活动时间参加。";
            }
            case APPROVED -> {
                subject = "报名已通过：《" + title + "》";
                content = "你对《" + title + "》的报名申请已通过。";
            }
            case REJECTED -> {
                subject = "报名未通过：《" + title + "》";
                content = "你对《" + title + "》的报名申请未通过。";
            }
            default -> {
                return;
            }
        }

        emailSender.send(new EmailMessage(
                event.getRegistrationId(), event.getType(), recipient, subject, content));
    }
}
