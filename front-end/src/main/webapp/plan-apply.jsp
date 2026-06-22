<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 申请旅行计划</title>
    <link rel="stylesheet" href="assets/css/app.css?v=<%= System.currentTimeMillis() %>">
</head>
<body class="form-body">
<nav class="global-nav">
    <div class="global-brand">公司旅行管理系统</div>
    <div id="userInfo" class="global-user"></div>
    <div class="global-actions">
        <button id="myAppsBtn" type="button">我的申请</button>
        <button id="passwordBtn" type="button">修改密码</button>
        <button id="logoutBtn" type="button">退出</button>
    </div>
</nav>
<nav class="sub-nav" aria-label="页面导览">
    <div class="sub-nav-inner">
        <a href="plans.jsp">旅游计划管理</a>
        <span class="sub-nav-separator">›</span>
        <strong>申请旅行计划</strong>
    </div>
</nav>
<main class="shell form-shell">
    <!-- 旅行计划申请页：员工提交或修改申请，URL 参数 planId 指定目标计划。 -->
    <section id="planApplyView" class="form-page plan-apply-page">
        <div class="form-card plan-edit-card plan-apply-card">
            <form id="planApplyForm" class="plan-edit-form plan-apply-form">
                <input type="hidden" name="planId">
                <input type="hidden" name="applicationId">
                <h1 class="plan-edit-title">申请旅行计划</h1>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>旅行计划</strong>
                        <span>请确认申请的计划信息</span>
                    </div>
                    <div id="applyPlanInfo" class="apply-plan-summary">
                        <div class="apply-summary-item">
                            <span>目的地</span>
                            <strong>加载中</strong>
                        </div>
                        <div class="apply-summary-item">
                            <span>行程时间</span>
                            <strong>加载中</strong>
                        </div>
                        <div class="apply-summary-item">
                            <span>价格</span>
                            <strong>加载中</strong>
                        </div>
                    </div>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>添加人员 <span class="required">*</span></strong>
                        <span>第一行为本人信息，可继续添加同行人员</span>
                    </div>
                    <div class="plan-apply-companions">
                        <div id="planApplyCompanionsRows" class="stack"></div>
                        <div class="person-divider" aria-hidden="true"></div>
                        <div id="planApplyExtraRows" class="stack"></div>
                        <button id="addPlanApplyCompanionBtn" type="button">新增随行人员</button>
                    </div>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>选项 / 备注</strong>
                        <span>填写座位、房型、餐食等需求</span>
                    </div>
                    <label class="field-only">
                        <span class="visually-hidden">选项 / 备注</span>
                        <textarea name="optionText" rows="3" placeholder="如座位、房型、餐食等需求"></textarea>
                    </label>
                </div>

                <p id="planApplyError" class="form-error hidden"></p>
                <div class="plan-form-footer">
                    <span>请确认申请信息后保存</span>
                    <div class="dialog-actions">
                        <button class="primary" type="submit">保存申请</button>
                        <button type="button" id="planApplyCancelBtn">取消</button>
                    </div>
                </div>
                <button id="planApplyBackBtn" class="hidden" type="button">返回列表</button>
            </form>
        </div>
    </section>
</main>

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
                <button id="oldPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
            </span>
        </label>
        <label>新密码
            <span class="password-field">
                <input name="newPassword" type="password" required minlength="6">
                <button id="newPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
            </span>
        </label>
        <label>确认密码
            <span class="password-field">
                <input name="confirmPassword" type="password" required minlength="6">
                <button id="confirmPasswordToggle" class="password-toggle hidden" type="button" aria-label="显示密码"></button>
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
<script src="assets/js/app.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
