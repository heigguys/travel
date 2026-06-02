package com.two.backend.controller;

import com.two.backend.dto.ApiResponse;
import com.two.backend.dto.LoginRequest;
import com.two.backend.dto.PasswordRequest;
import com.two.backend.model.User;
import com.two.backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
/**
 * 认证控制器，提供登录、退出、获取当前用户和修改密码接口。
 */
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    /**
     * 校验员工编号和密码，登录成功后把用户 ID 写入 Session。
     *
     * @param request 登录请求体
     * @param session 当前 HTTP 会话
     * @return 当前登录用户信息
     */
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.ok(authService.login(request, session));
    }

    @PostMapping("/logout")
    /**
     * 注销当前会话并清理登录状态。
     *
     * @param session 当前 HTTP 会话
     * @return 退出成功消息
     */
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.message("已退出登录");
    }

    @GetMapping("/me")
    /**
     * 获取当前 Session 对应的用户信息。
     *
     * @param session 当前 HTTP 会话
     * @return 当前用户信息，密码字段会被清空
     */
    public ApiResponse<User> me(HttpSession session) {
        User user = authService.currentUser(session);
        user.setPasswordMd5(null);
        return ApiResponse.ok(user);
    }

    @PostMapping("/password")
    /**
     * 修改当前登录用户的密码。
     *
     * @param request 修改密码请求体
     * @param session 当前 HTTP 会话
     * @return 修改成功消息
     */
    public ApiResponse<Void> password(@Valid @RequestBody PasswordRequest request, HttpSession session) {
        authService.changePassword(authService.currentUser(session), request);
        return ApiResponse.message("密码修改成功");
    }
}
