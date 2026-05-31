package com.two.backend.service;

import com.two.backend.dto.LoginRequest;
import com.two.backend.dto.PasswordRequest;
import com.two.backend.mapper.UserMapper;
import com.two.backend.model.Role;
import com.two.backend.model.User;
import com.two.backend.util.Md5Util;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public static final String SESSION_USER_ID = "USER_ID";
    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User login(LoginRequest request, HttpSession session) {
        User user = userMapper.findByEmployeeNo(request.employeeNo());
        if (user == null || !user.getPasswordMd5().equals(Md5Util.md5(request.password()))) {
            throw new BusinessException("员工编号或密码错误");
        }
        session.setAttribute(SESSION_USER_ID, user.getId());
        user.setPasswordMd5(null);
        return user;
    }

    public User currentUser(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER_ID);
        if (id == null) {
            throw new BusinessException("请先登录");
        }
        User user = userMapper.findById(Long.valueOf(id.toString()));
        if (user == null) {
            session.invalidate();
            throw new BusinessException("登录状态已失效");
        }
        return user;
    }

    public void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException("只有管理员可以执行该操作");
        }
    }

    public void changePassword(User user, PasswordRequest request) {
        if (!user.getPasswordMd5().equals(Md5Util.md5(request.oldPassword()))) {
            throw new BusinessException("原密码不正确");
        }
        userMapper.updatePassword(user.getId(), Md5Util.md5(request.newPassword()));
    }
}
