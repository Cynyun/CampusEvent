package com.Cynyun.campusevent.controller;

import com.Cynyun.campusevent.auth.AuthConstants;
import com.Cynyun.campusevent.auth.LoginUser;
import com.Cynyun.campusevent.common.ApiResponse;
import com.Cynyun.campusevent.entity.Message;
import com.Cynyun.campusevent.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/student/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ApiResponse<List<Message>> list(@SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        return ApiResponse.ok(messageService.list(user.id()));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id,
                                      @SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        messageService.markRead(user.id(), id);
        return ApiResponse.ok();
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        messageService.markAllRead(user.id());
        return ApiResponse.ok();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(@SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        return ApiResponse.ok(messageService.unreadCount(user.id()));
    }
}