package com.Cynyun.campusevent.auth;

import com.Cynyun.campusevent.common.BusinessException;
import com.Cynyun.campusevent.common.PasswordUtil;
import com.Cynyun.campusevent.entity.Admin;
import com.Cynyun.campusevent.entity.Student;
import com.Cynyun.campusevent.enums.Role;
import com.Cynyun.campusevent.mapper.AdminMapper;
import com.Cynyun.campusevent.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;

    public LoginUser login(String username, String password) {
        Student student = studentMapper.findByStudentId(username);
        if (student != null && PasswordUtil.matches(password, student.getPasswordHash())) {
            return new LoginUser(Role.STUDENT, student.getStudentId(), student.getName());
        }

        Admin admin = adminMapper.findByAdminId(username);
        if (admin != null && PasswordUtil.matches(password, admin.getPasswordHash())) {
            return new LoginUser(Role.ADMIN, admin.getAdminId(), admin.getName());
        }

        throw BusinessException.unauthorized("账号或密码错误");
    }
}