<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>公司旅行管理系统 - 旅行计划编辑</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
<main class="shell">
    <!-- 旅行计划编辑页：管理员新增或编辑旅行计划，URL 参数 id 存在时为编辑模式。 -->
    <section id="planEditView" class="form-page">
        <header class="topbar">
            <h1 id="planEditTitle">旅行计划</h1>
            <button id="planEditBackBtn" type="button">← 返回列表</button>
        </header>
        <div class="form-card">
            <form id="planEditForm" class="stack">
                <input type="hidden" name="id">
                <label>目的地 *
                    <input name="destination" required placeholder="如：敦煌莫高窟" maxlength="10">
                </label>
                <div class="grid2">
                    <label>启程日 *
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
                    <label>返回日 *
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
                <div class="grid2">
                    <label>价格（元）*
                        <input name="price" type="number" min="0" max="99999.99" step="0.01" required>
                    </label>
                    <label>定员数（人）*
                        <input name="capacity" type="number" min="1" required>
                    </label>
                </div>
                <div class="file-field">
                    <span class="file-label">PDF 附件</span>
                    <input id="planFileInput" class="visually-hidden-file" name="file" type="file" accept="application/pdf">
                    <div class="file-control">
                        <button id="planFileBtn" type="button">选择PDF</button>
                        <span id="planFileName" class="file-name">未选择文件</span>
                    </div>
                </div>
                <label class="inline">
                    <input name="published" type="checkbox"> 公开
                </label>
                <p id="planEditError" class="form-error hidden"></p>
                <div class="dialog-actions">
                    <button type="button" id="planEditCancelBtn">取消</button>
                    <button class="primary" type="submit">保存</button>
                </div>
            </form>
        </div>
    </section>
</main>
<div id="toast" class="toast hidden"></div>
<script src="assets/js/app.js"></script>
</body>
</html>
