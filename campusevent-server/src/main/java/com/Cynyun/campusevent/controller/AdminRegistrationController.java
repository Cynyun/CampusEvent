package com.Cynyun.campusevent.controller;

import com.Cynyun.campusevent.common.ApiResponse;
import com.Cynyun.campusevent.entity.RegistrationView;
import com.Cynyun.campusevent.enums.RegistrationStatus;
import com.Cynyun.campusevent.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/registrations")
@RequiredArgsConstructor
public class AdminRegistrationController {

    private final RegistrationService registrationService;

    @GetMapping
    public ApiResponse<List<RegistrationView>> list(@RequestParam(required = false) RegistrationStatus status,
                                                    @RequestParam(required = false) Long activityId) {
        return ApiResponse.ok(registrationService.registrationsForAdmin(status, activityId));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        registrationService.approve(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        registrationService.reject(id);
        return ApiResponse.ok();
    }
}