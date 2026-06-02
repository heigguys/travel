<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 登录</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
<main class="shell">
    <!-- 登录页：未登录用户通过员工编号和密码进入系统；已登录用户会自动跳转到 plans.jsp。 -->
    <section id="loginView" class="login-panel">
        <div>
            <p class="eyebrow">Travel Management</p>
            <h1>公司旅行管理系统</h1>
            <p class="muted">员工编号登录后可浏览、申请旅行计划并咨询管理员。</p>
        </div>
        <form id="loginForm" class="stack">
            <label>员工编号
                <input name="employeeNo" placeholder="A001 / U001" required>
            </label>
            <label>密码
                <span class="password-field">
                    <input name="password" type="password" placeholder="默认 123456" required>
                    <button id="loginPasswordToggle" class="password-toggle hidden" type="button" aria-label="show password">&#128065;&#65039;</button>
                </span>
            </label>
            <p id="loginError" class="form-error hidden">账号或密码错误</p>
            <button class="primary" type="submit">登录</button>
        </form>
        <p class="hint">示例账号：admin A001 / user U001，密码均为 123456</p>
    </section>
</main>

<!-- Toast 提示容器：用于显示登录失败等操作反馈。 -->
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
</body>
</html>
