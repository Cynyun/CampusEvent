package com.Cynyun.campusevent.service;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.entity.Message;
import com.Cynyun.campusevent.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public List<Message> list(String studentId) {
        return messageMapper.findByStudent(studentId);
    }

    @Transactional
    public void markRead(String studentId, Long id) {
        int rows = messageMapper.markRead(id, studentId);
        if (rows == 0) {
            throw BusinessException.notFound("通知不存在");
        }
    }

    @Transactional
    public void markAllRead(String studentId) {
        messageMapper.markAllRead(studentId);
    }

    public int unreadCount(String studentId) {
        return messageMapper.countUnread(studentId);
    }
}