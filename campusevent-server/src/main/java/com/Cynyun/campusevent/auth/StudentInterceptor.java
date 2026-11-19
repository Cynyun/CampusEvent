package com.Cynyun.campusevent.auth;

import com.Cynyun.campusevent.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class StudentInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser user = session == null ? null : (LoginUser) session.getAttribute(AuthConstants.SESSION_USER);
        if (user != null && user.role() == Role.STUDENT) {
            return true;
        }
        AuthInterceptor.write(response, 403, "该接口仅限学生使用");
        return false;
    }
}