<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 旅行计划编辑</title>
    <link rel="stylesheet" href="assets/css/app.css">
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
        <strong>旅游计划维护</strong>
    </div>
</nav>
<main class="shell form-shell">
    <!-- 旅行计划编辑页：管理员新增或编辑旅行计划，URL 参数 id 存在时为编辑模式。 -->
    <section id="planEditView" class="form-page">
        <div class="form-card plan-edit-card">
            <form id="planEditForm" class="plan-edit-form">
                <input type="hidden" name="id">
                <h1 id="planEditTitle" class="plan-edit-title">旅游计划添加</h1>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>目的地 <span class="required">*</span></strong>
                        <span>请输入旅游目的地</span>
                    </div>
                    <label class="field-only">
                        <span class="visually-hidden">目的地</span>
                        <input name="destination" required placeholder="如：敦煌莫高窟" maxlength="10">
                    </label>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>日期 <span class="required">*</span></strong>
                        <span>请选择行程日期</span>
                    </div>
                    <div class="plan-control-grid">
                        <label>启程日
                            <div class="date-field" id="startDateField">
                                <span class="date-placeholder">YYYY/MM/DD</span>
                                <input name="startYear" data-date-part="year" type="text" inputmode="numeric" maxlength="4">
                                <span class="date-sep">/</span>
                                <input name="startMonth" data-date-part="month" type="text" inputmode="numeric" maxlength="2">
                                <span class="date-sep">/</span>
                                <input name="startDay" data-date-part="day" type="text" inputmode="numeric" maxlength="2">
                                <button class="date-picker-btn" type="button" aria-label="选择启程日">
                                    <svg aria-hidden="true" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
                                </button>
                                <input class="date-native" type="date" tabindex="-1">
                            </div>
                        </label>
                        <label>返回日
                            <div class="date-field" id="endDateField">
                                <span class="date-placeholder">YYYY/MM/DD</span>
                                <input name="endYear" data-date-part="year" type="text" inputmode="numeric" maxlength="4">
                                <span class="date-sep">/</span>
                                <input name="endMonth" data-date-part="month" type="text" inputmode="numeric" maxlength="2">
                                <span class="date-sep">/</span>
                                <input name="endDay" data-date-part="day" type="text" inputmode="numeric" maxlength="2">
                                <button class="date-picker-btn" type="button" aria-label="选择返回日">
                                    <svg aria-hidden="true" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg>
                                </button>
                                <input class="date-native" type="date" tabindex="-1">
                            </div>
                        </label>
                    </div>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>费用与定员数 <span class="required">*</span></strong>
                        <span>设置费用和旅游团规模</span>
                    </div>
                    <div class="plan-control-grid">
                        <label>价格（元）
                            <input name="price" type="number" step="0.01" required>
                        </label>
                        <label>定员数（人）
                            <input name="capacity" type="number" required>
                        </label>
                    </div>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>附件</strong>
                        <span>上传计划相关文档</span>
                    </div>
                    <label id="planFileDropzone" class="upload-dropzone" for="planFileInput">
                        <span class="upload-file-copy">
                            <strong id="planFileText" class="upload-file-name">点击上传PDF文件</strong>
                            <span id="planFileHint" class="upload-file-hint">仅支持 PDF</span>
                        </span>
                        <input id="planFileInput" name="file" type="file" accept="application/pdf">
                    </label>
                </div>

                <div class="plan-form-row">
                    <div class="plan-row-label">
                        <strong>是否公开</strong>
                        <span>设置计划的可见性</span>
                    </div>
                    <div class="published-box">
                        <label class="radio-option">
                            <input name="published" type="radio" value="true"> 是
                        </label>
                        <label class="radio-option">
                            <input name="published" type="radio" value="false" checked> 否
                        </label>
                    </div>
                    <p class="plan-note">默认“否”，公开后计划对一般用户可见</p>
                </div>

                <p id="planEditError" class="form-error hidden"></p>
                <div class="plan-form-footer">
                    <span>所有带 <span class="required">*</span> 为必填项</span>
                    <div class="dialog-actions">
                        <button class="primary" type="submit">保存</button>
                        <button type="button" id="planEditCancelBtn">取消</button>
                    </div>
                </div>
                <button id="planEditBackBtn" class="hidden" type="button">返回列表</button>
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
<script src="assets/js/app.js"></script>
</body>
</html>
