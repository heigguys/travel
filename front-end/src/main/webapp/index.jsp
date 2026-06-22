<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 登录</title>
    <link rel="stylesheet" href="assets/css/app.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<main class="shell">
    <!-- 登录页：未登录用户通过员工编号和密码进入系统；已登录用户会自动跳转到 plans.jsp。 -->
    <section id="loginView" class="login-panel">
        <div>
            <p class="eyebrow">Travel Management</p>
            <h1>公司旅行管理系统</h1>
        </div>
        <form id="loginForm" class="stack">
            <label>员工编号
                <input name="employeeNo" required>
            </label>
            <label>密码
                <span class="password-field">
                    <input name="password" type="password" required>
                    <button id="loginPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
                </span>
            </label>
            <p id="loginError" class="form-error hidden"></p>
            <button class="primary" type="submit">登录</button>
        </form>
    </section>
</main>

<!-- Toast 提示容器：用于显示登录失败等操作反馈。 -->
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
