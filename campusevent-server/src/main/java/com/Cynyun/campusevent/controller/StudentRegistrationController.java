package com.Cynyun.campusevent.controller;

import com.Cynyun.campusevent.auth.AuthConstants;
import com.Cynyun.campusevent.auth.LoginUser;
import com.Cynyun.campusevent.common.ApiResponse;
import com.Cynyun.campusevent.entity.Registration;
import com.Cynyun.campusevent.entity.RegistrationView;
import com.Cynyun.campusevent.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/activities/{activityId}/registrations")
    public ApiResponse<Registration> register(@PathVariable Long activityId,
                                              @SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        return ApiResponse.ok(registrationService.register(user.id(), activityId));
    }

    @GetMapping("/registrations")
    public ApiResponse<List<RegistrationView>> myRegistrations(
            @SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        return ApiResponse.ok(registrationService.myRegistrations(user.id()));
    }

    @DeleteMapping("/registrations/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        registrationService.cancel(user.id(), id);
        return ApiResponse.ok();
    }
}