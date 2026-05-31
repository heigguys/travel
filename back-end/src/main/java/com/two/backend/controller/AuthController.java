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
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.ok(authService.login(request, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.message("已退出登录");
    }

    @GetMapping("/me")
    public ApiResponse<User> me(HttpSession session) {
        User user = authService.currentUser(session);
        user.setPasswordMd5(null);
        return ApiResponse.ok(user);
    }

    @PostMapping("/password")
    public ApiResponse<Void> password(@Valid @RequestBody PasswordRequest request, HttpSession session) {
        authService.changePassword(authService.currentUser(session), request);
        return ApiResponse.message("密码修改成功");
    }
}
