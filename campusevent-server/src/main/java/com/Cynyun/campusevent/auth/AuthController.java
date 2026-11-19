package com.Cynyun.campusevent.auth;

import com.Cynyun.campusevent.common.ApiResponse;
import com.Cynyun.campusevent.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginUser> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginUser user = authService.login(request.username(), request.password());
        session.setAttribute(AuthConstants.SESSION_USER, user);
        return ApiResponse.ok(user);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<LoginUser> me(@SessionAttribute(AuthConstants.SESSION_USER) LoginUser user) {
        return ApiResponse.ok(user);
    }
}