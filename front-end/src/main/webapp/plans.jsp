<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 旅游计划一览</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
<main class="shell">
    <!-- 旅游计划一览页：已登录用户查看和操作旅行计划，未登录会跳回 index.jsp。 -->
    <section id="appView" class="app">
        <header class="topbar">
            <div>
                <h1>旅游计划一览</h1>
                <p id="userInfo" class="muted"></p>
            </div>
            <div class="actions">
                <button id="myAppsBtn">我的申请</button>
                <button id="passwordBtn">修改密码</button>
                <button id="logoutBtn">退出</button>
            </div>
        </header>

        <!-- 筛选工具栏：按关键字、状态和排序方式查询旅行计划。 -->
        <section class="toolbar">
            <input id="keywordInput" placeholder="搜索目的地 / 计划编号">
            <select id="statusFilter">
                <option value="">全部状态</option>
                <option value="0">可申请</option>
                <option value="1">已成团</option>
                <option value="2">进行中</option>
                <option value="3">已结束</option>
                <option value="4">未成团</option>
            </select>
            <select id="sortSelect">
                <option value="">默认排序</option>
                <option value="startDate">按日期排序</option>
                <option value="price">按价格排序</option>
                <option value="capacity">按定员排序</option>
            </select>
            <button id="searchBtn" class="primary">搜索</button>
            <button id="newPlanBtn" class="primary hidden">添加旅游计划</button>
        </section>

        <!-- 旅行计划表格：表头和行数据由 app.js 根据后端返回动态渲染。 -->
        <div class="table-wrap">
            <table>
                <thead>
                <tr id="planHeader"></tr>
                </thead>
                <tbody id="planRows"></tbody>
            </table>
        </div>
        <div id="planPagination" class="pagination"></div>
    </section>
</main>

<!-- 随行人员弹窗：维护某条申请下的同行人员信息。 -->
<dialog id="companionsDialog">
    <form id="companionsForm" method="dialog" class="stack">
        <h2>修改随行人员信息</h2>
        <input type="hidden" name="applicationId">
        <div id="companionsRows" class="stack"></div>
        <button id="addCompanionBtn" type="button">新增随行人员</button>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存</button>
        </div>
    </form>
</dialog>

<!-- 咨询弹窗：展示计划咨询消息并发送新咨询。 -->
<dialog id="consultDialog">
    <form id="consultForm" method="dialog" class="stack">
        <h2>咨询管理</h2>
        <input type="hidden" name="planId">
        <div id="messages" class="messages"></div>
        <textarea name="content" rows="3" placeholder="输入咨询或回复内容" required></textarea>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>关闭</button>
            <button class="primary" type="submit">发送</button>
        </div>
    </form>
</dialog>

<!-- 我的申请弹窗：展示当前用户所有申请，并提供导出和取消入口。 -->
<dialog id="myAppsDialog">
    <div class="stack">
        <h2>我的申请</h2>
        <div id="myAppsRows" class="stack"></div>
        <div class="dialog-actions">
            <button id="exportPdfBtn" type="button">导出 PDF</button>
            <button type="button" data-close>关闭</button>
        </div>
    </div>
</dialog>

<!-- 修改密码弹窗：当前用户输入原密码和新密码完成密码变更。 -->
<dialog id="passwordDialog">
    <form id="passwordForm" method="dialog" class="stack">
        <h2>修改密码</h2>
        <label>原密码
            <span class="password-field">
                <input name="oldPassword" type="password" required>
                <button id="oldPasswordToggle" class="password-toggle hidden" type="button" aria-label="show password">&#128065;&#65039;</button>
            </span>
        </label>
        <label>新密码
            <span class="password-field">
                <input name="newPassword" type="password" required minlength="6">
                <button id="newPasswordToggle" class="password-toggle hidden" type="button" aria-label="show password">&#128065;&#65039;</button>
            </span>
        </label>
        <label>确认密码
            <span class="password-field">
                <input name="confirmPassword" type="password" required minlength="6">
                <button id="confirmPasswordToggle" class="password-toggle hidden" type="button" aria-label="show password">&#128065;&#65039;</button>
            </span>
        </label>
        <p id="passwordMessage" class="form-error hidden"></p>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存</button>
        </div>
    </form>
</dialog>

<!-- 删除确认弹窗：管理员删除计划前确认，并展示已有申请员工。 -->
<dialog id="deleteDialog">
    <form id="deleteForm" method="dialog" class="stack">
        <h2>删除旅行计划</h2>
        <input type="hidden" name="planId">
        <p id="deleteMessage" class="danger-note">确认删除该旅行计划？删除后将无法恢复。</p>
        <div>
            <strong>已有员工申请</strong>
            <div id="deleteApplicants" class="delete-preview-list"></div>
        </div>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button id="mailNotifyBtn" class="primary hidden" type="button">邮件通知</button>
            <button id="confirmDeleteBtn" class="danger" type="submit">确认删除</button>
        </div>
    </form>
</dialog>

<!-- Toast 提示容器：用于显示保存、删除、取消等操作反馈。 -->
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
</body>
</html>
