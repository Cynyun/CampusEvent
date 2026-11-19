package com.Cynyun.campusevent.event;

import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.entity.Message;
import com.Cynyun.campusevent.mapper.ActivityMapper;
import com.Cynyun.campusevent.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InAppNotificationObserver implements NotificationObserver {

    private final ActivityMapper activityMapper;
    private final MessageMapper messageMapper;

    @Override
    public void onEvent(RegistrationEvent event) {
        Activity activity = activityMapper.findById(event.getActivityId());
        String title = activity == null ? "活动" : "《" + activity.getTitle() + "》";
        String content = switch (event.getType()) {
            case SUBMITTED -> event.getStatus() == com.Cynyun.campusevent.enums.RegistrationStatus.PENDING
                    ? "报名已提交，等待审核：" + title
                    : "报名成功：" + title;
            case APPROVED -> "报名已通过：" + title;
            case REJECTED -> "报名未通过：" + title;
            case CANCELLED -> "报名已取消：" + title;
        };

        Message message = new Message();
        message.setStudentId(event.getStudentId());
        message.setActivityId(event.getActivityId());
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);
        messageMapper.insert(message);
    }
}