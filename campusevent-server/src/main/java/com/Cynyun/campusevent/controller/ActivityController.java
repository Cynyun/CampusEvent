package com.Cynyun.campusevent.controller;

import com.Cynyun.campusevent.auth.AuthConstants;
import com.Cynyun.campusevent.auth.LoginUser;
import com.Cynyun.campusevent.common.ApiResponse;
import com.Cynyun.campusevent.entity.Activity;
import com.Cynyun.campusevent.enums.PolicyType;
import com.Cynyun.campusevent.enums.Role;
import com.Cynyun.campusevent.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ApiResponse<List<Activity>> list(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) PolicyType policyType,
                                            @RequestParam(required = false) String category) {
        return ApiResponse.ok(activityService.listActive(keyword, policyType, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Activity> detail(@PathVariable Long id,
                                        @SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        if (user.role() == Role.STUDENT) {
            return ApiResponse.ok(activityService.getForStudent(id, user.id()));
        }
        return ApiResponse.ok(activityService.getAny(id));
    }
}