package com.two.backend.service;

import com.two.backend.dto.LoginRequest;
import com.two.backend.dto.PasswordRequest;
import com.two.backend.mapper.UserMapper;
import com.two.backend.model.User;
import com.two.backend.util.Md5Util;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
/**
 * 认证服务，集中处理登录状态、角色校验和密码修改。
 */
public class AuthService {
    public static final String SESSION_USER_ID = "USER_ID";
    private final UserMapper userMapper;

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 校验员工编号和密码，成功后把用户 ID 保存到 Session。
     *
     * @param request 登录请求
     * @param session 当前 HTTP 会话
     * @return 已隐藏密码摘要的用户信息
     */
    public User login(LoginRequest request, HttpSession session) {
        User user = userMapper.findByEmployeeNo(request.employeeNo());
        if (user == null || !user.getPasswordMd5().equals(Md5Util.md5(request.password()))) {
            throw new BusinessException("员工编号或密码错误");
        }
        session.setAttribute(SESSION_USER_ID, user.getId());
        user.setPasswordMd5(null);
        return user;
    }

    /**
     * 根据 Session 中的用户 ID 获取当前登录用户。
     *
     * @param session 当前 HTTP 会话
     * @return 当前启用用户
     */
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

    /**
     * 校验当前用户是否为管理员。
     *
     * @param user 当前用户
     */
    public void requireAdmin(User user) {
        if (!Integer.valueOf(User.ROLE_ADMIN).equals(user.getRole())) {
            throw new BusinessException("只有管理员可以执行该操作");
        }
    }

    /**
     * 校验旧密码并保存新密码摘要。
     *
     * @param user 当前用户
     * @param request 修改密码请求
     */
    public void changePassword(User user, PasswordRequest request) {
        String oldPasswordMd5 = Md5Util.md5(request.oldPassword());
        String newPasswordMd5 = Md5Util.md5(request.newPassword());
        if (!user.getPasswordMd5().equals(oldPasswordMd5)) {
            throw new BusinessException("原密码错误");
        }
        if (oldPasswordMd5.equals(newPasswordMd5)) {
            throw new BusinessException("新密码不能和旧密码相同");
        }
        userMapper.updatePassword(user.getId(), newPasswordMd5);
    }
}
