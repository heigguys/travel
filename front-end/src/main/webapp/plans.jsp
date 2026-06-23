<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 旅游计划一览</title>
    <link rel="stylesheet" href="assets/css/app.css?v=<%= System.currentTimeMillis() %>">
</head>
<body class="plans-body">
<nav class="global-nav">
    <div class="global-brand">公司旅行管理系统</div>
    <div id="userInfo" class="global-user"></div>
    <div class="global-actions">
        <button id="consultMessagesBtn" class="consult-nav-btn" type="button" title="咨询消息">
            <svg aria-hidden="true" viewBox="0 0 24 24">
                <path d="M21 12a8.5 8.5 0 0 1-8.5 8.5 9.2 9.2 0 0 1-3.2-.58L3 21l1.34-4.7A8.5 8.5 0 1 1 21 12Z"></path>
                <path d="M8 11h8M8 15h5"></path>
            </svg>
            <span>咨询消息</span>
            <span id="consultUnreadBadge" class="nav-badge hidden"></span>
        </button>
        <button id="myAppsBtn" type="button">我的申请</button>
        <button id="passwordBtn" type="button">修改密码</button>
        <button id="logoutBtn" type="button">退出</button>
    </div>
</nav>
<nav class="sub-nav" aria-label="页面导览">
    <div class="sub-nav-inner">
        <a href="index.jsp?showLogin=1">登录</a>
        <span class="sub-nav-separator">›</span>
        <strong>旅游计划一览</strong>
    </div>
</nav>
<main class="shell plans-shell">
    <!-- 旅游计划一览页：已登录用户查看和操作旅行计划，未登录会跳回 index.jsp。 -->
    <section id="appView" class="app">
        <!-- 筛选工具栏：按关键字和状态查询旅行计划。 -->
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
            <select id="publishedFilter" class="hidden">
                <option value="">公开状态</option>
                <option value="true">已公开</option>
                <option value="false">未公开</option>
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
    <form id="consultForm" method="dialog" class="consult-panel">
        <div class="consult-header">
            <h2>咨询管理</h2>
            <button value="cancel" type="button" data-close>关闭</button>
        </div>
        <input type="hidden" name="planId">
        <input type="hidden" name="participantUserId">
        <div class="consult-layout">
            <aside class="consult-sidebar">
                <div class="consult-column-title">咨询员工</div>
                <div id="consultSessions" class="consult-sessions"></div>
            </aside>
            <section class="consult-plan-panel">
                <div class="consult-column-title">旅游计划编号</div>
                <div id="consultPlanNo" class="consult-plan-no"></div>
            </section>
            <section class="consult-chat-panel">
                <div id="messages" class="messages consult-messages"></div>
                <div class="consult-input-row">
                    <textarea name="content" rows="3" placeholder="输入咨询或回复内容" required></textarea>
                    <button class="primary consult-send-btn" type="submit">发送</button>
                </div>
            </section>
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

<%@ include file="WEB-INF/jsp/fragments/password-dialog.jspf" %>

<!-- 删除确认弹窗：管理员删除计划前确认，并展示已有申请员工。 -->
<dialog id="deleteDialog">
    <form id="deleteForm" method="dialog" class="delete-dialog-panel">
        <div class="delete-dialog-header">
            <div>
                <h2>删除旅行计划</h2>
                <p>删除前请确认申请情况。该操作完成后不可恢复。</p>
            </div>
        </div>
        <input type="hidden" name="planId">
        <div class="delete-dialog-body">
            <div class="delete-warning-card">
                <strong>删除提醒</strong>
                <p id="deleteMessage">确认删除该旅行计划？删除后将无法恢复。</p>
            </div>
            <div class="delete-applicants-panel">
                <div class="delete-section-title">
                    <strong>已有员工申请</strong>
                    <span id="deleteApplicantSummary">暂无员工申请</span>
                </div>
                <div id="deleteApplicants" class="delete-preview-list"></div>
            </div>
        </div>
        <div class="dialog-actions delete-dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button id="mailNotifyBtn" class="primary hidden" type="button">邮件通知</button>
            <button id="confirmDeleteBtn" class="danger" type="submit">确认删除</button>
        </div>
    </form>
</dialog>

<!-- Toast 提示容器：用于显示保存、删除、取消等操作反馈。 -->
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
