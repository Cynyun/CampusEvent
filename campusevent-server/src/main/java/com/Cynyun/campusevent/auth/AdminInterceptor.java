package com.Cynyun.campusevent.auth;

import com.Cynyun.campusevent.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser user = session == null ? null : (LoginUser) session.getAttribute(AuthConstants.SESSION_USER);
        if (user != null && user.role() == Role.ADMIN) {
            return true;
        }
        AuthInterceptor.write(response, 403, "没有管理员权限");
        return false;
    }
}