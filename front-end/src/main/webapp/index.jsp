<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
<main class="shell">
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

    <section id="appView" class="app hidden">
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

        <section class="toolbar">
            <input id="keywordInput" placeholder="搜索目的地 / 计划编号">
            <select id="statusFilter">
                <option value="">全部状态</option>
                <option value="未开始">未开始</option>
                <option value="进行中">进行中</option>
                <option value="已结束">已结束</option>
            </select>
            <select id="sortSelect">
                <option value="">默认排序</option>
                <option value="startDate">按日期排序</option>
                <option value="price">按价格排序</option>
                <option value="capacity">按定员排序</option>
            </select>
            <button id="searchBtn">搜索</button>
            <button id="newPlanBtn" class="primary hidden">添加旅游计划</button>
        </section>

        <div class="table-wrap">
            <table>
                <thead>
                <tr id="planHeader"></tr>
                </thead>
                <tbody id="planRows"></tbody>
            </table>
        </div>
    </section>
</main>

<dialog id="planDialog">
    <form id="planForm" method="dialog" class="stack">
        <h2 id="planDialogTitle">旅行计划</h2>
        <input type="hidden" name="id">
        <label>目的地 *
            <input name="destination" required placeholder="如：敦煌莫高窟">
        </label>
        <div class="grid2">
            <label>启程日 *
                <input name="startDate" type="date" required>
            </label>
            <label>返回日 *
                <input name="endDate" type="date" required>
            </label>
        </div>
        <div class="grid2">
            <label>价格（元）*
                <input name="price" type="number" min="0" step="0.01" required>
            </label>
            <label>定员数（人）*
                <input name="capacity" type="number" min="1" required>
            </label>
        </div>
        <label>PDF 附件
            <input name="file" type="file" accept="application/pdf">
        </label>
        <label class="inline">
            <input name="published" type="checkbox"> 公开
        </label>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存</button>
        </div>
    </form>
</dialog>

<dialog id="applyDialog">
    <form id="applyForm" method="dialog" class="stack">
        <h2>申请旅行计划</h2>
        <input type="hidden" name="planId">
        <label>申请人数 *
            <input name="applicantCount" type="number" min="1" required>
        </label>
        <label>选项 / 备注
            <textarea name="optionText" rows="3" placeholder="如座位、房型、餐食等需求"></textarea>
        </label>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存申请</button>
        </div>
    </form>
</dialog>

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

<dialog id="consultDialog">
    <form id="consultForm" method="dialog" class="stack">
        <h2>咨询管理</h2>
        <input type="hidden" name="planId">
        <div id="messages" class="messages"></div>
        <textarea name="content" rows="3" placeholder="输入咨询或回复内容" required></textarea>
        <div class="dialog-actions">
            <button id="closeConsultBtn" type="button">结束对话</button>
            <button value="cancel" type="button" data-close>关闭</button>
            <button class="primary" type="submit">发送</button>
        </div>
    </form>
</dialog>

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
        <p id="passwordMessage" class="form-error hidden"></p>
        <div class="dialog-actions">
            <button value="cancel" type="button" data-close>取消</button>
            <button class="primary" type="submit">保存</button>
        </div>
    </form>
</dialog>

<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
</body>
</html>
